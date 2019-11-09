# 多媒体学习之路 android
主体框架分为`app`主模块作为入口；`baselib`作为最基本的库，给各个模块引用，定义了一些接口和封装Util类，比如permission，MyLog，ThreadUtils等。

#### 第一课：_1learn  学习Bitmap Canvas Draw

包含了三种View加载本地文件bitmap的学习。了解ImageView，SurfaceView，自定义View，配合Canvas绘制bitmap。

#### 第二课：_2learn  学习AudioRecord与MediaRecord录制音频

`ISimpleRecord`（普通录音） 和`IRecord` （支持暂停与恢复录音）2个抽象接口表示需要实现的2种模式。

0. `simpleMediaRecord`包实现了IRecord，使用MediaRecord录音，操作相对简单，录制出来的东西就已经是编码过的可以播放的文件了；有条件地支持暂停与恢复(api24 MediaRecord开始支持pause/resume)；（推荐）

1. `simpleAudioRecordV1`包实现了ISimpleRecord，是按照网络上的一些攻略实现的普通录音，开始，停止，先录制出来pcm，是不能播放的；然后录制完成以后，先做一个header，再拷贝一次原pcm文件，拼接成wav；(不推荐)

2. `simpleAudioRecordV2`包实现了ISimpleRecord，代码上与V1差不多，只是因为V1需要在完成后再拷贝一次，如果文件过大，我认为是浪费性能的；改成RandomAccessFile在录制之前添加好header，录制完成后seek文件指针，将length写入即可；（推荐）

3. `AudioRecordV3`包实现了IRecord，在V2的基础上，添加支持暂停与恢复。

   3.0是一个通过lock实现的简单等待和恢复(不推荐)；
   
   3.1是通过文件直接拼接后续的录音buffer而做的(推荐)。

#### 第三课：_3learn  学习audio相关的播放

1. soundPool适用于简单的音效播放；
2. mediaPlayer实现了简单的监听完成，监听拖动，暂停，恢复等基本使用；支持多种媒体类型；
3. audioTrack TODO