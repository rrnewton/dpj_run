
{- | The benchmarking script:

USAGE:

   ./run_benchmark [mode] [hsbencher options]

Where mode is '--desktop', '--server', or '--quick'.

-}

{-# LANGUAGE NamedFieldPuns #-}
{-# OPTIONS_GHC -fwarn-incomplete-patterns #-}

module Main where

import GHC.Conc           (getNumProcessors)
import System.Environment (getEnvironment, getArgs, withArgs)
import System.IO.Unsafe   (unsafePerformIO)
import System.Console.GetOpt

import HSBencher.Types(BenchSpace(..), Benchmark(..), ParamSetting(..), DefaultParamMeaning(..),
                       Config(..), mkBenchmark
                                                                        
                       -- compileOptsOnly, enumerateBenchSpace, toCompileFlags,
                       -- makeBuildID, BuildID, 
                      )
import HSBencher.App (defaultMainModifyConfig, all_cli_options)

----------------------------------------
-- For the custom build method:
{-
import System.Directory
import HSBencher.Types
import HSBencher.Logging (log)
import HSBencher.Utils (runLogged, defaultTimeout)

import Control.Monad
import Control.Monad.Reader
import qualified Data.ByteString.Char8 as B
import qualified Data.Map as M
-- import Control.Monad.IO.Class (liftIO, MonadIO)
import System.Process
-- import System.Directory
import System.FilePath
import Prelude hiding (log)
-}

--------------------------------------------------------------------------------
-- Main Script
--------------------------------------------------------------------------------

data Mode = Server | Desktop | Quick      deriving (Show,Eq)
data Flag = SetMode Mode
          | WorkMultiplier Double
          | Help
          deriving (Show,Eq)
                
options :: [OptDescr Flag]
options =
    [ Option [] ["server"]  (NoArg (SetMode Server))  "server-sized benchmarks"
    , Option [] ["desktop"] (NoArg (SetMode Desktop)) "desktop-sized benchmarks"
    , Option [] ["quick"]   (NoArg (SetMode Quick))   "(default) quick testing"
    , Option [] ["amplify"] (ReqArg (WorkMultiplier . read) "DOUBLE")
                            "multiply the amount of work to do by a coefficient"
    , Option ['h'] ["help"] (NoArg Help)              "report this help message"
    ]

main :: IO ()
main = do
  args <- getArgs
  let (opts,nonopts,unrecog,errs) = getOpt' Permute options args
  let help1 = usageInfo ("USAGE: run_benchmark [options]\n"++
                        "\nFirst, specific options for this script are:\n")
                options
      help2 = usageInfo (help1++"\nAlso use the generic HSBencher options below:\n")
                        (concat $ map snd all_cli_options)
  if Help `elem` opts || errs /= [] then
    error help2
   else do
    let passthru = nonopts ++ unrecog
        modes    = [ s | SetMode s <- opts ]
        coef     = case [ s | WorkMultiplier s <- opts ] of
                     [] -> 1.0
                     [d] -> toRational d
                     ls -> error$"more than one --amplify param: "++show ls
                     
    putStrLn$ "  [Bench script mode selection]: "++ show modes
    putStrLn$ "  [Note: passing through options to HSBencher]: "++unwords passthru
    withArgs passthru $
     let benches = case modes of
                     [Desktop] -> (bls_desktop coef)
                     [Server]  -> (bls_server coef)
                     [Quick]   -> (bls_quick )
                     []        -> (bls_quick )
                     ls        -> error$ "Conflicting mode options: "++show ls
     in defaultMainModifyConfig $ \conf -> 
         conf { benchlist = benches
              , argsBeforeFlags = False
              }
-- defaultMainWithBechmarks

--------------------------------------------------------------------------------
-- Here are the actual benchmarks:
--------------------------------------------------------------------------------

bls_quick :: [Benchmark DefaultParamMeaning]
bls_quick =
 [ mkBenchmark "dpjbin/Benchmarks/Kernels/" ["TEST","1024"] dpjSettings
-- , Benchmark "dpjbin/Benchmarks/Kernels/" ["TEST","1024"] dpjSettings
 , mkBenchmark "./cilksort/" [] cilkSettings
 ]

bls_desktop :: Rational -> [Benchmark DefaultParamMeaning]
bls_desktop coef =
 let sz :: Integer
     sz = round$ coef * defaultSortSize in
 [ Benchmark { target="dpjbin/Benchmarks/Kernels/"
             , cmdargs= ["TIME",show sz]
             , configs = dpjSettings
             , progname = Just "dpj_MergeSort4"
             }
 , mkBenchmark "./cilksort/" ["-n",show sz] cilkSettings
--  , Benchmark "dpjbin/Benchmarks/Kernels/" ["TIME",show sz] dpjSettings 
 ]

bls_server :: Rational -> [Benchmark DefaultParamMeaning]
bls_server = bls_desktop -- Same for now.

defaultSortSize :: Rational
defaultSortSize = 2^26 -- 67 million elements

--------------------------------------------------------------------------------
-- Set up some common benchmark config spaces:
--------------------------------------------------------------------------------

-- | Add any default settings into the config.
dpjSettings :: BenchSpace DefaultParamMeaning
dpjSettings = varyThreads (And [])

cilkSettings :: BenchSpace DefaultParamMeaning
cilkSettings = varyThreadsCilk (And [])

--------------------------------------------------------------------------------
-- Supporting definitions:
--------------------------------------------------------------------------------

-- TODO: make this a command-line option:
threadSelection :: [Int]
threadSelection = unsafePerformIO $ do
  env <- getEnvironment
  p   <- getNumProcessors
  case lookup "THREADS" env of
    Just ls -> return$ map read $ words ls
    -- Arbitrary default policy 
    Nothing
      | p <= 16   -> return  [1 .. p]
      | otherwise -> return$ 1 : [2,4 .. p]

-- unsafeEnv = unsafePerformIO getEnvironment

-- | Add variation from thread count.    
varyThreads :: BenchSpace DefaultParamMeaning -> BenchSpace DefaultParamMeaning
varyThreads conf = Or (map fn threadSelection)
 where
   -- HORRIBLE HACK:
   fn n = Or [ Set (Threads n) $ RuntimeParam  ("MergeSort4 --dpj-num-threads "++ show n)
--             , Set (Threads n) $ RuntimeParam  ("MergeSort8 --dpj-num-threads "++ show n)
             ]

varyThreadsCilk :: BenchSpace DefaultParamMeaning -> BenchSpace DefaultParamMeaning
varyThreadsCilk conf = Or (map fn threadSelection)
 where
   fn n = Set (Threads n) (RuntimeEnv "CILK_NWORKERS" (show n))

--------------------------------------------------------------------------------
-- Creating a (tweaked) custom build method for this suite
--------------------------------------------------------------------------------   

{-
-- | Build with GNU Make.  Assumes a "make run" target.
makeMethod :: BuildMethod
makeMethod = BuildMethod
  { methodName = "make"
  , canBuild = (IsExactly "Makefile")
               `PredOr`
               InDirectoryWithExactlyOne (IsExactly "Makefile")
  , concurrentBuild = False
  , setThreads      = Nothing
  , clean = \ pathMap _ target -> do
     doMake pathMap target $ \ makePath -> do
       _ <- runSuccessful subtag (makePath++" clean")
       return ()
  , compile = \ pathMap bldid flags target -> do
     doMake pathMap target $ \ makePath -> do
       absolute <- liftIO getCurrentDirectory
       _ <- runSuccessful subtag (makePath++" COMPILE_ARGS='"++ unwords flags ++"'")
       log$ tag++"Done building with Make, assuming this benchmark needs to run in-place..."
       let runit args envVars =
             CommandDescr
             { command = ShellCommand (makePath++" run RUN_ARGS='"++ unwords args ++"'")
             , timeout = Just defaultTimeout
             , workingDir = Just absolute
             , envVars
             }
       return (RunInPlace runit)
  }
 where
  tag = " [makeMethod] "
  subtag = " [make] "
  doMake pathMap target action = do
     isdir <- liftIO$ doesDirectoryExist target
     let dir = if isdir then target
               else takeDirectory target
         makePath = M.findWithDefault "make" "make" pathMap
     inDirectory dir (action makePath)


--------------------------------------------------------------------------------

-- | A simple wrapper for a command that is expected to succeed (and whose output we
-- don't care about).  Throws an exception if the command fails.
runSuccessful :: String -> String -> BenchM [B.ByteString]
runSuccessful tag cmd = do
  (res,lins) <- runLogged tag cmd
  case res of
    ExitError code  -> error$ "expected this command to succeed! But it exited with code "++show code++ ":\n  "++ cmd
    RunTimeOut {}   -> error "Methods.hs/runSuccessful - internal error!"
    RunCompleted {} -> return lins

inDirectory :: (MonadIO m) => FilePath -> m a -> m a
inDirectory dir act = do 
  orig <- liftIO$ getCurrentDirectory
  liftIO$ setCurrentDirectory dir
  x <- act
  liftIO$ setCurrentDirectory orig
  return x
-- TODO: Use bracket, but it's only IO, not generalized:
  -- bracket (do o <- liftIO getCurrentDirectory
  --             setCurrentDirectory dir
  --             return o)
  --         (\orig -> liftIO$ setCurrentDirectory orig)
  --         (\_ -> act)
  
-- Returns actual files only
filesInDir :: FilePath -> IO [FilePath]
filesInDir d = do
  inDirectory d $ do
    ls <- getDirectoryContents "."
    filterM doesFileExist ls

-}
