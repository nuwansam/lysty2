pushd %~dp0

set argC=0
for %%x in (%*) do Set /A argC+=1

if %argC% == 0 (
	start javaw -jar lysty.jar 
) else (
	if %argC% == 1 (
		start javaw -jar lysty.jar %1
	) else (
		if %argC% == 2 (
			start javaw -jar lysty.jar %1 %2
		} else (
			echo incorrect number of parameters
		)
	)
)
exit

REM start javaw -jar lysty.jar %1

