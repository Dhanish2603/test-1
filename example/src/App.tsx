import { useState, useEffect } from 'react';
import {
  StyleSheet,
  View,
  Text,
  Button,
  PermissionsAndroid,
} from 'react-native';
import {  locationfind } from 'react-native-location-module';

export default function App() {
  const [result, setResult] = useState<string>();

  useEffect(() => {
    // multiply(3, 7).then(setResult);
  }, []);
  const find = async () => {
    try {
      const res = await PermissionsAndroid.requestMultiple([
        'android.permission.ACCESS_FINE_LOCATION',
        'android.permission.ACCESS_COARSE_LOCATION',
        'android.permission.ACCESS_BACKGROUND_LOCATION',
        'android.permission.ACCESS_MEDIA_LOCATION',
      ]);
      console.log('Rs', res);
      const data = await locationfind();
      console.log(data);
      setResult(JSON.stringify(data))
    } catch (error) {
      console.log('error', error);
      setResult(JSON.stringify(error))
    }
  };

  return (
    <View style={styles.container}>
      <Text>Result: {result}</Text>
      <Button title="Click here" onPress={find} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
