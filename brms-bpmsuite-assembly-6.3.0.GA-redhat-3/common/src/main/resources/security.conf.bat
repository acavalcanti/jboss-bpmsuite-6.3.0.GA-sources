rem # Enforce security policy to avoid malicious java code running 
set "SECMGR=true"
set "DIRNAME_VAL=%DIRNAME%"
set "ESCAPED=file:///"

:nextval
for /F "tokens=1,* delims= " %%a in ("%DIRNAME_VAL%") do (
set "CHUNK=%%a"
set "DIRNAME_VAL=%%b"
)
set "ESCAPED=%ESCAPED%%CHUNK%"
if defined DIRNAME_VAL set "ESCAPED=%ESCAPED%%%20" & goto nextval

set "ESCAPED=%ESCAPED:\=/%"
set "JAVA_OPTS=%JAVA_OPTS% -Djboss.modules.policy-permissions=true -Djava.security.policy=%ESCAPED%security.policy -Dkie.security.policy=%ESCAPED%kie.policy"

