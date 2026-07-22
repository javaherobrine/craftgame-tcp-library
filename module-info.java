/**
 * Module for craftgame-tcp library
 */
open module org.craftgame.network{
	requires transitive static java.desktop;//For debugger
	requires java.base;
	exports CC0;
	exports CC0.UDPClient;
	exports io.github.javaherobrine;
	exports io.github.javaherobrine.net;
	exports io.github.javaherobrine.net.proxy;
	exports io.github.javaherobrine.net.speed;
	exports io.github.javaherobrine.net.tls;
	exports io.github.javaherobrine.net.ui;
}