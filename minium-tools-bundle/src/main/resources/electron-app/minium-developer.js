var _child_process = require('child_process'),
    _fs = require('fs'),
    _http = require('http'),
    _os = require('os'),
    _path = require('path'),
    _yaml = require('js-yaml');

var electron,
    process,
    url,
    window;

var close = (() => {
  var thereAreUnsavedFiles;

  return (event) => {
    if (thereAreUnsavedFiles === undefined || thereAreUnsavedFiles) {
      event.preventDefault();
    }

    if (thereAreUnsavedFiles === undefined) {
      window.webContents.executeJavaScript(`angular.element(document.body).injector().get('MiniumEditor').areDirty()`, false, (result) => { thereAreUnsavedFiles = result; });
    }

    function showDialogIfThereAreUnsavedFiles() {
      if (thereAreUnsavedFiles === undefined) {
          setTimeout(showDialogIfThereAreUnsavedFiles, 100);
      } else {
        if (thereAreUnsavedFiles) {
          electron.dialog.showMessageBox(window, {
              type: 'warning',
              buttons: ['Yes', 'Cancel'],
              title: 'There are unsaved files',
              message: 'There are unsaved files, are you sure that you want to close Minium Developer?'
          }, (response) => {
            if (response == 0) {
              shutdown();
            } else {
              thereAreUnsavedFiles = undefined;
            }
          });
        } else {
          shutdown();
        }
      }
    }

    showDialogIfThereAreUnsavedFiles();
  }
})();

function executeScript(path, args, options) {
  if (isWindows()) {
    return _child_process.spawn('cmd', ['/c', _path.join(_path.dirname(process.execPath), path)].concat(args), options);
  } else {
    return _child_process.spawn('sh', [_path.join(_path.dirname(process.execPath), path)].concat(args), options);
  }
}

function isMiniumDeveloperRunning(callback) {
  _http.get(url, (response) => {
    callback(true);
  }).on('error', (e) => {
    callback(false);
  });
}

function isWindows() {
  return _os.platform() === "win32";
}

function openMiniumDeveloperWhenReady() {
  isMiniumDeveloperRunning((isRunning) => {
    if (isRunning) {
      window.loadURL(url);
      window.on('close', close);
    } else {
      setTimeout(openMiniumDeveloperWhenReady, 2500);
    }
  });
}

function shutdown() {
  if (process) {
    if (isWindows()) {
      _child_process.spawnSync("taskkill", ["/pid", process.pid, '/f', '/t']);
    } else {
      process.kill();
    }
  }

  window.removeListener('close', close);
  electron.app.quit();
}

module.exports = {
  automate: (args) => {
    if (isWindows()) {
      executeScript('bin/minium-automator.bat', args, {
        detached: true,
        stdio: 'ignore'
      }).unref();
    } else {
      executeScript('bin/minium-automator', args, {
        stdio: 'inherit'
      });
    }
  },
  launch: (_electron, _window) => {
    electron = _electron;
    window = _window;

    window.title = 'Minium Developer';
    window.loadURL('file://' + _path.join(__dirname, 'static/loading.html'));

    var conf = _yaml.safeLoad(_fs.readFileSync(_path.join(_path.dirname(process.execPath), 'config/application.yml'), 'utf8'));
    url = 'http://localhost:' + conf.server.port;

    isMiniumDeveloperRunning((isRunning) => {
      if (!isRunning) {
        process = isWindows() ?
          executeScript('bin/minium-developer.bat', [], { cwd: _path.dirname(process.execPath) })
          :	executeScript('bin/minium-developer', [], { cwd: _path.dirname(process.execPath) });
      } else {
        shutdown();
      }
    });

    openMiniumDeveloperWhenReady();
  },
  launchInBrowser: () => {
    if (isWindows()) {
      executeScript('bin/minium-developer-browser.bat', [], {
        detached: true,
        cwd: _path.dirname(process.execPath),
        stdio: 'ignore'
      }).unref();
    } else {
      executeScript('bin/minium-developer-browser', [], {
        cwd: _path.dirname(process.execPath),
        stdio: 'inherit'
      });
    }
  }
};
