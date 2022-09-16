import TrackPlayer from 'react-native-track-player';

export const storagePath = 'file:///storage/emulated/0/Music/Srrk/';

export const init = () => {
  TrackPlayer.setupPlayer();
};


export const play = async (name, path) => {
  const music = {
    url: 'file://' + path,
    title: name,
    duration: 0,
  };
  const list = await TrackPlayer.getQueue();
  await TrackPlayer.remove(Array.from(Array(list.length), (_, index) => index));
  await TrackPlayer.add([music])
  await TrackPlayer.setVolume(1);
  await TrackPlayer.play();
};
