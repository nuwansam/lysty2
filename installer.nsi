; Lysty.nsi
;
; This script is based on example1.nsi, but it remember the directory, 
; has uninstall support and (optionally) installs start menu shortcuts.
;
; It will install Lysty.nsi into a directory that the user selects,

;--------------------------------

; The name of the installer
Name "Lysty"

; The file to write
OutFile "Lysty.exe"

; The default installation directory
InstallDir $PROGRAMFILES\Lysty

; Registry key to check for directory (so if you install again, it will 
; overwrite the old one automatically)
InstallDirRegKey HKLM "Software\Lysty" "Install_Dir"

; Request application privileges for Windows Vista
RequestExecutionLevel admin

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
  File /r "db"
  File /r "lysty_lib"
  File /r "plugins"
  File /r "resources"
  File /r "sqls"
  File "lysty.jar"
  File "lysty.exe"
  
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
