package auto.wechat.appium.com.appiumwechatauto;

import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private String PACKAGE_NAME = "com.tencent.mm";

    private TextView mDeviceTxt;

    private final String testStr = "关于纹理的性能|WebGL地图引擎系列第五期\n"+ "2017年6月2日原创";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDeviceTxt = (TextView) findViewById(R.id.device_info_txt);

        StringBuilder sb = new StringBuilder();
        sb.append("系统版本：" + getSystemVersion() + "\n");
        sb.append("设备名称：" + android.os.Build.MODEL + "\n");

        PackageInfo info = getWeChatInfo();
        if(info != null){
            sb.append("安装路径：" + info.applicationInfo.sourceDir+ "\n");
            sb.append("进程名称：" + info.applicationInfo.processName+ "\n");
            ActivityInfo[] activityInfos = info.activities;
            if(activityInfos != null){
                int size = activityInfos.length;
                for(int i=0 ;i< size;i++){
                    sb.append("页面信息：" + activityInfos[i].toString()+ "\n");
                }
            }
        }
        mDeviceTxt.setText(sb.toString());
    }

    private PackageInfo getWeChatInfo(){
        PackageManager packageManager = getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = null;
        try {
            packInfo = packageManager.getPackageInfo(PACKAGE_NAME,0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packInfo;
    }

    /**
     * 获取相应安装包的路径
     *
     * @return
     */
    public String getInstallPath(){
        try{
            ApplicationInfo info = getPackageManager().getApplicationInfo(PACKAGE_NAME, 0);
            return info.sourceDir;
        }catch (Exception e){
            return null;
        }
    }

    /**
     * 获取当前手机系统版本号
     *
     * @return  系统版本号
     */
    public static String getSystemVersion() {
        return android.os.Build.VERSION.RELEASE;
    }
}
