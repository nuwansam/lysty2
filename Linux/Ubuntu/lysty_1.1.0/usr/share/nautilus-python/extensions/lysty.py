import subprocess
import os

from gi.repository import Nautilus, GObject

class LystyPlayNextExtension(GObject.GObject, Nautilus.MenuProvider):
    def __init__(self):
        pass

    def menu_activate_cb(self, menu, file):
	fileUri=file.get_uri()
	subprocess.call(["/usr/bin/lysty","play_next",fileUri[7:].replace('%20',' ')])

    def get_file_items(self, window, files):
        if len(files) != 1:
            return
        
        file = files[0]
        
        if not file.is_mime_type("audio/*"):
            return

	
        item = Nautilus.MenuItem(
            name="LystyMenuExtension::LystyPlayNext",
            label="Play Next in Lysty",
            tip="Play Next in Lysty"
        )
        item.connect('activate', self.menu_activate_cb, file)
        
        return [item]

class LystyEnqueueExtension(GObject.GObject, Nautilus.MenuProvider):
    def __init__(self):
        pass

    def menu_activate_cb(self, menu, file):
	fileUri=file.get_uri()
	subprocess.call(["/usr/bin/lysty","enqueue",fileUri[7:].replace('%20',' ')])

    def get_file_items(self, window, files):
        if len(files) != 1:
            return
        
        file = files[0]
        
        if not file.is_mime_type("audio/*"):
            return

        item = Nautilus.MenuItem(
            name="LystyMenuExtension::LystyEnqueue",
            label="Enqueue in Lysty",
            tip="Enqueue in Lysty"
        )
        item.connect('activate', self.menu_activate_cb, file)
        
        return [item]
