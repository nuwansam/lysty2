; Lysty.nsi
;
; This script is based on example1.nsi, but it remember the directory, 
; has uninstall support and (optionally) installs start menu shortcuts.
;
; It will install Lysty.nsi into a directory that the user selects,

;--------------------------------

; The name of the installer
Name "Lysty Installer"

; The file to write
OutFile "Lysty-Install.exe"

; The default installation directory
InstallDir $PROGRAMFILES\Lysty

; Registry key to check for directory (so if you install again, it will 
; overwrite the old one automatically)
InstallDirRegKey HKLM "Software\Lysty" "Install_Dir"

; Request application privileges for Windows Vista
RequestExecutionLevel admin

!define StrRep "!insertmacro StrRep"
!macro StrRep output string old new
    Push `${string}`
    Push `${old}`
    Push `${new}`
    !ifdef __UNINSTALL__
        Call un.StrRep
    !else
        Call StrRep
    !endif
    Pop ${output}
!macroend
 
!macro Func_StrRep un
    Function ${un}StrRep
        Exch $R2 ;new
        Exch 1
        Exch $R1 ;old
        Exch 2
        Exch $R0 ;string
        Push $R3
        Push $R4
        Push $R5
        Push $R6
        Push $R7
        Push $R8
        Push $R9
 
        StrCpy $R3 0
        StrLen $R4 $R1
        StrLen $R6 $R0
        StrLen $R9 $R2
        loop:
            StrCpy $R5 $R0 $R4 $R3
            StrCmp $R5 $R1 found
            StrCmp $R3 $R6 done
            IntOp $R3 $R3 + 1 ;move offset by 1 to check the next character
            Goto loop
        found:
            StrCpy $R5 $R0 $R3
            IntOp $R8 $R3 + $R4
            StrCpy $R7 $R0 "" $R8
            StrCpy $R0 $R5$R2$R7
            StrLen $R6 $R0
            IntOp $R3 $R3 + $R9 ;move offset by length of the replacement string
            Goto loop
        done:
 
        Pop $R9
        Pop $R8
        Pop $R7
        Pop $R6
        Pop $R5
        Pop $R4
        Pop $R3
        Push $R0
        Push $R1
        Pop $R0
        Pop $R1
        Pop $R0
        Pop $R2
        Exch $R1
    FunctionEnd
!macroend
!insertmacro Func_StrRep ""
!insertmacro Func_StrRep "un."

Function WriteToFile
Exch $0 ;file to write to
Exch
Exch $1 ;text to write
 
  FileOpen $0 $0 a #open file
  FileSeek $0 0 END #go to end
  FileWrite $0 $1 #write to file
  FileClose $0
 
Pop $1
Pop $0
FunctionEnd
 
!macro WriteToFile NewLine File String
  !if `${NewLine}` == true
  Push `${String}$\r$\n`
  !else
  Push `${String}`
  !endif
  Push `${File}`
  Call WriteToFile
!macroend
!define WriteToFile `!insertmacro WriteToFile false`
!define WriteLineToFile `!insertmacro WriteToFile true`


;; Function that register one extension for Lysty
Function RegisterExtension
  ; back up old value for extension $R0 (eg. ".opt")
  ReadRegStr $1 HKCR "$R0" ""
  StrCmp $1 "" NoBackup
    StrCmp $1 "Lysty$R0" "NoBackup"
    WriteRegStr HKCR "$R0" "Lysty.backup" $1
NoBackup:
  WriteRegStr HKCR "$R0" "" "Lysty$R0"
  ReadRegStr $0 HKCR "Lysty$R0" ""
  WriteRegStr HKCR "Lysty$R0" "" "Lysty media file ($R0)"
  WriteRegStr HKCR "Lysty$R0\shell" "" "PlayNow"
  WriteRegStr HKCR "Lysty$R0\shell\Play" "" "PlayThisNow"
  WriteRegStr HKCR "Lysty$R0\shell\Play\command" "" '"$INSTDIR\lysty.exe"  "%1"'
 WriteRegStr HKCR "Lysty$R0\DefaultIcon" "" '"$INSTDIR\resources\icons\lysty.png",0'
 
;;; Vista Only part
  ; Vista detection
  ReadRegStr $R1 HKLM "SOFTWARE\Microsoft\Windows NT\CurrentVersion" CurrentVersion
  StrCpy $R2 $R1 3
  StrCmp $R2 '6.0' ForVista ToEnd
ForVista:
  WriteRegStr HKLM "Software\Clients\Media\Lysty\Capabilities\FileAssociations" "$R0" "Lysty$R0"
 
ToEnd:
FunctionEnd
 
;; Function that removes one extension that Lysty owns.
Function un.RegisterExtension
  ;start of restore script
  ReadRegStr $1 HKCR "$R0" ""
  StrCmp $1 "Lysty$R0" 0 NoOwn ; only do this if we own it
    ; Read the old value from Backup
    ReadRegStr $1 HKCR "$R0" "Lysty.backup"
    StrCmp $1 "" 0 Restore ; if backup="" then delete the whole key
      DeleteRegKey HKCR "$R0"
    Goto NoOwn
Restore:
      WriteRegStr HKCR "$R0" "" $1
      DeleteRegValue HKCR "$R0" "Lysty.backup"
NoOwn:
    DeleteRegKey HKCR "Lysty$R0" ;Delete key with association settings
    DeleteRegKey HKLM "Software\Clients\Media\Lysty\Capabilities\FileAssociations\Lysty$R0" ; for vista
FunctionEnd

;--------------------------------

; Pages

Page components
Page directory
Page instfiles

UninstPage uninstConfirm
UninstPage instfiles

;--------------------------------

; The stuff to install
Section "Install Files"

  SectionIn RO
  
  ; Set output path to the installation directory.
  SetOutPath $INSTDIR
  
  ; Put file there
  File /r "config"
  File /r "lib"
  File /r "resources"
  File /r "sqls"
  File "lysty.jar"
  File "lysty.exe"
  
  ${StrRep} '$0' '$APPDATA' '\' '/'
  ; write the db,logs,settings folder paths to the config
  ${WriteLineToFile} `$INSTDIR\config\config.properties` `db_dir=$0/Lysty/db`
  ${WriteLineToFile} `$INSTDIR\config\config.properties` `plugins_dir=$0/Lysty/plugins`
  ${WriteLineToFile} `$INSTDIR\config\config.properties` `logs_dir=$0/Lysty/logs`
  ;${WriteLineToFile} `$INSTDIR/config/config.properties` `settings_file=$0/Lysty/settings/settings.properties'
 ${WriteLineToFile} `$INSTDIR\config\config.properties` `settings_file=$0/Lysty/settings/settings.properties`
  
 SetOutPath $APPDATA\Lysty
  File /r "settings"
  File /r "plugins"
  
  ; Write the installation path into the registry
  WriteRegStr HKLM SOFTWARE\Lysty "Install_Dir" "$INSTDIR"
  
  ; Write the uninstall keys for Windows
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Lysty" "DisplayName" "Lysty"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Lysty" "UninstallString" '"$INSTDIR\uninstall.exe"'
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Lysty" "NoModify" 1
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Lysty" "NoRepair" 1
  WriteUninstaller "uninstall.exe"
  
  
WriteRegStr HKCR Applications\Lysty.exe "" ""
WriteRegStr HKCR Applications\Lysty.exe "FriendlyAppName" "Lysty media player"
WriteRegStr HKCR Applications\Lysty.exe\shell\PlayNext "" "Play Next in Lysty"
WriteRegStr HKCR Applications\Lysty.exe\shell\PlayNext\command "" \
    '"$INSTDIR\lysty.exe" play_next "%1"'
WriteRegStr HKCR Applications\Lysty.exe\shell\Queue "" "Add to Lysty Play"
WriteRegStr HKCR Applications\Lysty.exe\shell\Queue\command "" \
    '"$INSTDIR\lysty.exe" enqueue "%1"'
WriteRegStr HKCR Applications\Lysty.exe\shell\Play "" "Play in Lysty"
WriteRegStr HKCR Applications\Lysty.exe\shell\Play\command "" \
    '"$INSTDIR\lysty.exe" "%1"'
WriteRegStr HKCR Applications\Lysty.exe\SupportedTypes .mp3 ""
WriteRegStr HKCR Applications\Lysty.exe\SupportedTypes .wma ""

;file ext association for .ppl files
WriteRegStr HKCR lysty.ppl "" "Lysty Partial Playlist (.ppl)"
WriteRegStr HKCR lysty.ppl\shell "" "Open"
WriteRegStr HKCR lysty.ppl\shell\OpenInLysty "" "Open In Lysty"
WriteRegStr HKCR lysty.ppl\shell\OpenInLysty\command "" \
    '"$INSTDIR\lysty.exe" "%1"'
WriteRegStr HKCR .ppl "" "lysty.ppl"
	
 StrCpy $R0 ".mp3"
   Call RegisterExtension
	
 SectionEnd

; Optional section (can be disabled by the user)
Section "Start Menu Shortcuts"

  CreateDirectory "$SMPROGRAMS\Lysty"
  CreateShortCut "$SMPROGRAMS\Lysty\Uninstall.lnk" "$INSTDIR\uninstall.exe" "" "$INSTDIR\uninstall.exe" 0
  CreateShortCut "$SMPROGRAMS\Lysty\Lysty.lnk" "$INSTDIR\lysty.exe" "" "$INSTDIR\lysty.exe" 0
  
SectionEnd

;--------------------------------

; Uninstaller

Section "Uninstall"
  
  ; Remove registry keys
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Lysty"
  DeleteRegKey HKLM SOFTWARE\Lysty
  DeleteRegKey HKCR Applications\Lysty.exe
  DeleteRegKey HKCR .ppl 
  DeleteRegKey HKCR lysty.ppl
  
  StrCpy $R0 ".mp3"
   Call un.RegisterExtension

  ; Remove files and uninstaller
  RMDir /r $INSTDIR
  Delete $INSTDIR\uninstall.exe

  ; Remove shortcuts, if any
  Delete "$SMPROGRAMS\Lysty\*.*"

  ; Remove directories used
  RMDir "$SMPROGRAMS\Lysty"
  RMDir "$INSTDIR"

SectionEnd
