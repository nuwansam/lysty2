Lysty is a music player designed to be less intrusive for the user by providing 2 key features:

Infini Play:

On InfiniPlay mode, when the song(s) you played manually finish, Lysty will find matching songs that suits the flow and add them to the current playlist (and again, when those songs also complete and so on). This way, you don't have to keep on brainstorming songs to play next all the time. There are various strategies to match and find songs to play next. you can change the strategy used by selecting one from the strategy combobox in the player.

Partial Playlists: - Where cup half filled is better than complete

Creating playlists require a lot of effort to find matching songs and creating a neat flow. Whats worse, the playlists are static - you have to listen to the same playlist again and again. With lystys' Parrtial Playlists, you fill the playlist only partially by dragging and dropping songs from your music folders to the desired locations in the playlist. The half baked playlists can be saved and loaded as well (in .ppl format). The partial playlists are filled and played on the fly, and as such, every time you load the same .ppl file, it will be a different playlist that will be generated (with the overall general structure of the playlist kept intact defined as per your song selection). whats more, this enables you to have as much control as you wish since you can decide how much of the playlist you fill manually.

Strategies:

Currently there are 3 strategies implemented. Strategies are plugins to the system, and as such, anyone can create a strategy and add it to the application.

Random: This is the simplest of the strategies. It simply adds songs randomly.
	Available settings: Common folder height: The number of folder levels up from the seed song to search for candidate songs. i.e: If you set this to 2, and you manually added a song at location: /songs/english/artist1/album1/, the random strategy will add songs only within /songs/english/artist1.

MetaTagMatch: This strategy finds songs which has exact matches of a selected meta tag of the selected song(s). For instance, if you select the feature to match on to be "artist", then this strategy will add songs of the same artist(s) as of the seed songs.
	Available settings: Feature to match on: The meta feature that will be used to find matching songs. The supported meta features are: Artist, Album, Release Year, Language, Key, Genre, Mood, Composer, Is Compilation.

Cumulative Meta Match: This is the most versatile of the available strategies. It calculates a weighted score for the suitability of a song to the playlist by considering all the IDv3 meta tags available for the songs. The weightages for each meta feature can be configured using the settings for this strategy.
	Available settings:
		Artist Weight: the weightage to use for artist meta feature
		Album Weight: the weightage to use for album meta feature
		Release Year Weight: the weightage to use for release year meta feature
		Genre Weight: the weightage to use for genre meta feature
		Mood Weight: the weightage to use for mood meta feature
		Lanugage Weight: the weightage to use for language meta feature
		Composer Weight: the weightage to use for composer meta feature
		Common Folder Height: The number of folder levels up from the seed song(s) to search for additional songs.
		Folder Distance Weight: The weightage to be used for the distance from the seed song to the candidate song (the number of folder levels to reach the common parent of the seed song and candidate song from the seed song level)
		
=====================License Information===============================================

Lysty music player is released under GNU General Public License.