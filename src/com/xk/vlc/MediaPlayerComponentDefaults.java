package com.xk.vlc;

public class MediaPlayerComponentDefaults {
	static String[] EMBEDDED_MEDIA_PLAYER_ARGS = {
        "--video-title=vlcj video output",
        "--no-snapshot-preview",
        "--quiet",
        "--intf=dummy"
    };

    static String[] AUDIO_MEDIA_PLAYER_ARGS = {
        "--quiet",
        "--intf=dummy"
    };

    private MediaPlayerComponentDefaults() {
    }
}
