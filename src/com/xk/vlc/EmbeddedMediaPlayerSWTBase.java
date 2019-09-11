package com.xk.vlc;

import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import uk.co.caprica.vlcj.media.Media;
import uk.co.caprica.vlcj.media.MediaEventListener;
import uk.co.caprica.vlcj.media.MediaParsedStatus;
import uk.co.caprica.vlcj.media.MediaRef;
import uk.co.caprica.vlcj.media.Meta;
import uk.co.caprica.vlcj.media.Picture;
import uk.co.caprica.vlcj.media.TrackType;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventListener;
import uk.co.caprica.vlcj.player.base.State;

public class EmbeddedMediaPlayerSWTBase extends Canvas implements MediaPlayerEventListener, MediaEventListener{

	public EmbeddedMediaPlayerSWTBase(Composite parent, int style) {
		super(parent, style);
		
	}

	@Override
	public void mediaMetaChanged(Media media, Meta metaType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mediaSubItemAdded(Media media, MediaRef newChild) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mediaDurationChanged(Media media, long newDuration) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mediaParsedChanged(Media media, MediaParsedStatus newStatus) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mediaFreed(Media media, MediaRef mediaFreed) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mediaStateChanged(Media media, State newState) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mediaSubItemTreeAdded(Media media, MediaRef item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mediaThumbnailGenerated(Media media, Picture picture) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mediaChanged(MediaPlayer mediaPlayer, MediaRef media) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void opening(MediaPlayer mediaPlayer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void buffering(MediaPlayer mediaPlayer, float newCache) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void playing(MediaPlayer mediaPlayer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void paused(MediaPlayer mediaPlayer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopped(MediaPlayer mediaPlayer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void forward(MediaPlayer mediaPlayer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void backward(MediaPlayer mediaPlayer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void finished(MediaPlayer mediaPlayer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void positionChanged(MediaPlayer mediaPlayer, float newPosition) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void seekableChanged(MediaPlayer mediaPlayer, int newSeekable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pausableChanged(MediaPlayer mediaPlayer, int newPausable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void titleChanged(MediaPlayer mediaPlayer, int newTitle) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void snapshotTaken(MediaPlayer mediaPlayer, String filename) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void lengthChanged(MediaPlayer mediaPlayer, long newLength) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void videoOutput(MediaPlayer mediaPlayer, int newCount) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void scrambledChanged(MediaPlayer mediaPlayer, int newScrambled) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void elementaryStreamAdded(MediaPlayer mediaPlayer, TrackType type,
			int id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void elementaryStreamDeleted(MediaPlayer mediaPlayer,
			TrackType type, int id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void elementaryStreamSelected(MediaPlayer mediaPlayer,
			TrackType type, int id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void corked(MediaPlayer mediaPlayer, boolean corked) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void muted(MediaPlayer mediaPlayer, boolean muted) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void volumeChanged(MediaPlayer mediaPlayer, float volume) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void audioDeviceChanged(MediaPlayer mediaPlayer, String audioDevice) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void chapterChanged(MediaPlayer mediaPlayer, int newChapter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void error(MediaPlayer mediaPlayer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mediaPlayerReady(MediaPlayer mediaPlayer) {
		// TODO Auto-generated method stub
		
	}

}
