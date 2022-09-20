import TrackPlayer from 'react-native-track-player';

export const storagePath = '/storage/emulated/0/Music/Srrk/';
export const remoteUrl = 'https://srrk.s3.ap-northeast-2.amazonaws.com/audio/';

export const init = async () => {
  try {
    await TrackPlayer.setupPlayer();
  } catch (e) {
    if (e.toString().includes('already')) {
      return;
    }
  }
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

export const close = async () => {
  // await TrackPlayer.close();
}