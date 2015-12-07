# IPCamServer for android

This is using android device camera simulate as an IP Camera.
The http M-Jpeg Stream can be used by ffmpeg/vlc as client to view.
http://ipaddress:8080/ for web browser only. If you want to get
the M-Jpeg Stream directly, please use http://ipaddress:8080/video.cgi.
The HTTP Server supports multiple access, I have test for 5 clients and
over 24hrs. 

* for web browser : http://ipaddress:8080/
* for vlc/ffplay/ffmpeg : http://ipaddress:8080/video.cgi




