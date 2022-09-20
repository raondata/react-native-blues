import RNFS from 'react-native-fs';

export const read = (path = RNFS.DocumentDirectoryPath) => {
  return RNFS.readDir(path)
    .then((items) => {
      console.log('storage>> GOT RESULTS', items);
      return items
        .filter(item => item.isFile())
        .map(item => ({ id: item.name, name: item.name, path: item.path }));
    })
    .catch((err) => {
      console.log(err.message, err.code);
    });
};

export const listRemote = (path) => {
  return [
    'asmr1.mp3',
    'vocgan.wav',
  ].map(filename => ({
    id: filename.split('.')[0],
    name: filename,
    path: path + filename
  }));
}