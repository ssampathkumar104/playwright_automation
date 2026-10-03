@echo off
REM ============================================
REM Run Maven tests with a specified test plan
REM Usage: run-test-plan.bat <PlanName>
REM Example: run-test-plan.bat SamplePlan
REM The .xml extension is appended automatically.
REM ============================================

if "%~1"=="" (
    echo ERROR: No test plan specified.
    echo Usage: %~nx0 ^<TestPlanName^>
    echo Example: %~nx0 SamplePlan
    exit /b 1
)

echo Running test plan: %~1.xml
mvn clean install -DTEST_PLAN="%~1.xml"
