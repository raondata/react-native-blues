import { StyleSheet } from "react-native";

export const commonStyles = StyleSheet.create({
  container: {
    flex: 1,
    flexDirection: 'column',
    padding: 20,
  },
  header: {
    flex: -1,
    flexDirection: 'row',
    justifyContent: 'flex-start',
    alignItems: 'center',
    marginBottom: 10,
  },
  body: {
    flex: 3,
  },
  list: {
    borderWidth: 1,
    borderColor: '#ccc',
  },
  item: {
    backgroundColor: '#eee',
    flex: 1,
    flexDirection: 'column',
    margin: 5,
    padding: 5,
    borderWidth: 1,
    borderColor: '#ccc',
    borderRadius: 10,
  },
  itemTitle: {
    color: 'black',
    fontSize: 12,
  },
  itemSubtitle: {
    color: '#777',
    fontSize: 11
  },
  footer: {
    justifyContent: 'flex-start',
    alignItems: 'flex-start',
    marginTop: 15
  },
  inputGroup: {
    flex: -1,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    height: 40,
    paddingVertical: 5,
  },
  inputLabel: {
    textAlign: 'right',
    width: 80,
    fontSize: 12,
    marginRight: 7,
  },
  input: {
    flex:1,
    borderRadius: 5,
    borderWidth: 1,
    fontSize: 13,
    padding: 5,
    borderColor: '#ccc'
  },
  btn: {
    borderWidth: 1,
    borderRadius: 7,
    borderColor: '#eee',
    backgroundColor: '#349beb',
    padding: 5,
  },
  btnText: {
    color: "#eee",
  },
});
