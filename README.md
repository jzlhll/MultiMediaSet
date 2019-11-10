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
   
   >  Android SDK 提供两套音频采集API，MediaRecorder 和 AudioRecord，前者可以直接把麦克风录入的音频数据进行编码压缩（如AMR、MP3等）并存成文件；后者更接近底层，更加自由灵活，可以得到原始的一帧帧PCM音频数据。如果想简单地做一个录音机，推荐使用 MediaRecorder；而如果需要对音频做进一步的算法处理、或者采用第三方的编码库进行压缩、以及网络传输等应用，则建议使用 AudioRecord，其实 MediaRecorder 底层也是调用了 AudioRecord 与 Android Framework 层的 AudioFlinger 进行交互的。直播中实时采集音频自然是要用`AudioRecord`。 
   >
   >  **修正： 重新查阅wav格式。修正PCM2WavUtil代码中的Header错误的构建方式**。

#### 第三课：_3learn  学习audio相关的播放

MediaPlayer 更加适合在后台长时间播放本地音乐文件或者在线的流式资源; SoundPool 则适合播放比较短的音频片段，比如游戏声音、按键声、铃声片段等等，它可以同时播放多个音频; 而 AudioTrack 则更接近底层，提供了非常强大的控制能力，支持低延迟播放，适合流媒体和VoIP语音电话等场景。

1. `soundPool` 适用于简单的音效播放；

2. `mediaPlayer` 实现了简单的监听完成，监听拖动，暂停，恢复等基本使用；支持多种媒体类型；

3. `audioTrack` TODO

   > > 区别
   >
   > MediaPlayer可以播放多种格式MP3，AAC，WAV，OGG，MIDI等。MediaPlayer会在framework层创建对应的音频解码器。而AudioTrack只能播放已解码的PCM流，只支持wav(大部分wav是PCM流)格式的音频文件。AudioTrack不创建解码器，只能播放不需要解码的wav文件。
   >
   > > 联系
   >
   > MediaPlayer在framework层还是会创建AudioTrack，把解码后的PCM数流传递给AudioTrack，再传递给AudioFlinger进行混音，然后才传递给硬件播放，所以是MediaPlayer包含了AudioTrack。

