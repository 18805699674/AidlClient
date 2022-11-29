package cn.iichen.aidlclient

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.Parcel
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cn.iichen.aidlserver.IRemoteService
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedReader
import java.io.FileDescriptor
import java.io.FileReader


class MainActivity : AppCompatActivity() {
    lateinit var iRemoteService:IRemoteService
    var isBind:Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        plus.setOnClickListener {
            if(isBind) {
                iRemoteService.run {
                    // 加法
                    val a = plus_value_a.text.toString()
                    val b = plus_value_b.text.toString()
                    if(a.isNotBlank() && b.isNotBlank()) {
                        val result = plus(a.toInt(),b.toInt())
                        Toast.makeText(this@MainActivity,"结果 $result",Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this,"当前未注册",Toast.LENGTH_SHORT).show()
            }
        }

        bindService.setOnClickListener {
            if(!isBind) {
                val intent = Intent()
                intent.action = "cn.iichen.aidlserver.remote"
                intent.setPackage("cn.iichen.aidlserver")
                bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE)
            } else {
                Toast.makeText(this,"当前已注册",Toast.LENGTH_SHORT).show()
            }
        }

        unBindService.setOnClickListener {
            if(isBind) {
                unbindService(serviceConnection)
            } else {
                Toast.makeText(this,"当前未注册",Toast.LENGTH_SHORT).show()
            }
        }


    }
    private val serviceConnection:ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
//            iRemoteService = IRemoteService.Stub.asInterface(service)
//            isBind = true
//            Toast.makeText(this@MainActivity,"注册成功！",Toast.LENGTH_SHORT).show()

            // 共享内存的形式获取 大数据
            val data = Parcel.obtain()
            val reply = Parcel.obtain()
            try {
                //通过binder机制跨进程调用服务端的接口
                service.transact(1, data, reply, 0)
                //获得RemoteService创建的匿名共享内存的fd
                val fd: FileDescriptor = reply.readFileDescriptor().fileDescriptor
                //读取匿名共享内存中的数据，并打印log
                val br = BufferedReader(FileReader(fd))
                Log.d("iichen", br.readLine())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBind = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
    }
}