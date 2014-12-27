try {
    require("source-map-support").install();
} catch(err) {
}
require("./out/goog/bootstrap/nodejs.js");
require("./out/server.js");
goog.require("server.core");
goog.require("cljs.nodejscli");
