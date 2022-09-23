import TrackPlayer, { Event, State } from 'react-native-track-player';

let wasPausedByDuck = false;

const playbackServiceEvents = {};

const addEvent = (evt, handler) => {
  playbackServiceEvents[evt] = TrackPlayer.addEventListener(evt, handler);
};

const registerEvents = (map) => {
  Object.entries(map).forEach(([evt, handler]) => addEvent(evt, handler));
};

export async function PlaybackService() {
  registerEvents({
    [Event.RemotePause]: () => { TrackPlayer.pause(); },
    [Event.RemotePlay]: () => { TrackPlayer.play(); },
    [Event.RemoteNext]: () => { TrackPlayer.skipToNext(); },
    [Event.RemotePrevious]: () => { TrackPlayer.skipToPrevious(); },
    [Event.RemoteDuck]: async ({permanent, paused}) => {
      if (permanent) {
        TrackPlayer.pause();
        return;
      }
      if (paused) {
        const playerState = await TrackPlayer.getState();
        wasPausedByDuck = playerState !== State.Paused;
        TrackPlayer.pause();
      } else {
        if (wasPausedByDuck) {
          TrackPlayer.play();
          wasPausedByDuck = false;
        }
      }
    },
    [Event.PlaybackQueueEnded]: (event) => { console.log('Event.PlaybackQueueEnded', event); },
    [Event.PlaybackTrackChanged]: (event) => { console.log('Event.PlaybackTrackChanged', event); },
    [Event.PlaybackProgressUpdated]: (event) => { console.log('Event.PlaybackProgressUpdated', event); },
    [Event.RemoteSeek]: (event) => { console.log('Event.RemoteSeek', event); },
  });
}
