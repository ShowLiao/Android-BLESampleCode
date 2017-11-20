# Android-BLESampleCode
Compile環境設定:
設定app/build.gradle
dependencies {
  compile fileTree(include: ['*.jar'], dir: 'libs')
  testCompile 'junit:junit:4.12'
  compile 'com.android.support:appcompat-v7:25.2.0'
  compile 'com.orhanobut:logger:1.15'
  compile files(‘libs/gson-2.2.4.jar')        <-路徑請確認自己的環境
  compile files(‘libs/vpbluetooth_1.0.1.jar') <-路徑請確認自己的環境
  compile files(‘libs/vpprotocol_1.0.1.jar')  <-路徑請確認自己的環境
} 

AndroidMenifest.xml
必須加入BlueTooth的相關Permission給系統。
在安裝時系統都是看這個設定檔才知道要跟User要Permission
說明: 
在安裝App時，User 會看到 App會要幾項Permission，若是User不同意，那App就不會裝到手機上。
PS.開發時，若是需要的Permission沒有加到此檔案中，在run code時會直接Crash

依照Doc 以下的參數必須加入到此XML file 中

<uses-permission android:name="android.permission.BLUETOOTH" />

<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />

<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

<uses-feaure
  android:name="android.hardware.bluetooth_le"
  android:required="true" />
  
<service android:name="com.inuker.bluetooth.library.BluetoothService" />
->這個service 如果app/build.gradle沒有加depandencies tag : compile ‘com.orhanobut:logger:1.15’ Compile 會error


How to do:
import 相關 ble lib: 
import com.veepoo.protocol.VPOperateManager; 
import com.veepoo.protocol.listener.base.IABleConnectStatusListener;
import com.veepoo.protocol.listener.base.IABluetoothStateListener;
VPOperateManager 是主要的Object
使用getMangerInstance() 取得instance之後才可以對它操作

Click Button說明 : 詳細使用請參考Sample
id: ScanBtn : onScanClick : Scan BLE Device
id: ConnDevice : onConnectDeviceClick : 必須要先執行 ScanBtn 才可以做這個動作
id: BPDetectBtn : onDetectClick: 偵測血壓
id: BPDetectStopBtn: onDetectStopClick : 停止Detect血壓
id: WalkBtn : onWalkClick : 取得目前步數
