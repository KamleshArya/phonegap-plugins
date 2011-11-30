var Dropbox = function() {}

/** @constructor */
var DropboxError = function() {
   this.code = null;
   this.message = null;
};

DropboxError.AUTHENTICATION_ERR = 1;
DropboxError.NETWORK_ERR = 1;

Dropbox.prototype.login = function(username, password, success, fail) {
    if (username === null || typeof username === "undefined") {
        if (typeof fail === "function") {
            fail({code: 1, message: "No username specified."})
        }
    }
    if (password === null || typeof password === "undefined") {
        if (typeof fail === "function") {
            fail({code: 1, message: "No password specified."})
        }
    }
    var options = {};
    options.username = username;
    options.password = password;

    PhoneGap.exec(success, fail, 'Dropbox', 'login', [options]);
};

Dropbox.prototype.logout = function(success, fail) {
    PhoneGap.exec(success, fail, 'Dropbox', 'logout', []);
};

Dropbox.prototype.accountInfo = function(success, fail) {
    PhoneGap.exec(success, fail, 'Dropbox', 'accountInfo', []);
};

PhoneGap.addConstructor(function() {
    PhoneGap.addPlugin('dropbox', new Dropbox());
});