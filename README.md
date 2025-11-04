# CraftGame TCP Library
## craftgame-tcp library is independent after April 18th 2021
It still synchronizes with repository javaherobrine/CraftGame and it used to be a part of CraftGame

It includes a C/S library and a TCP connection debugger which allows you to send and receive plain text directly. You can use it to send a HTTP request, send an email through a SMTP server, etc.

Also, it may help you deal with SSL/TLS certificates.

Releases are provided, but it's recommended to embed my code into your code directly under MIT License.

`new Thread(new SocketUI(new Socket(host.port))).start();` works.

~[Document](https://javaherobrine.github.io/document/tcp)~ Need update(It's the legacy, the version that I deleted)

If you discovered this repository in 2021, you may know what I'm saying

Note: some of UDP code was not written by CraftGame Studio and maybe there will be some implementations that differ from Java_Herobrine's(in package io.gitub.javaherobrine) or LovelyZeeiam's(in package xueli) (and their subpackages).