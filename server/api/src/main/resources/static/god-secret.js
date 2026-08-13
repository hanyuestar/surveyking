/**
 * god-secret.js - 外挂密码重置入口（独立注入脚本，不依赖 React / umi chunk）
 *
 * 功能：
 *   1. 请求 GET /api/system 读取 isGodSecretEnabled；未启用则不做任何事
 *   2. 启用时在登录页右上角 .lang 元素旁插入钥匙按钮
 *   3. 点击弹出 Modal（外挂密码 / 账户名 / 新密码），提交 POST /api/public/resetPassword
 *
 * 安全约束：
 *   - 本脚本不包含任何外挂密码明文常量
 *   - 不将外挂密码写入 console / localStorage / URL
 *   - 仅使用原生 DOM API，只追加节点，不修改 React 管理的 DOM 结构
 */
(function () {
  'use strict';

  var NS = 'gs-';
  var STYLE_ID = NS + 'style';
  var MODAL_ID = NS + 'modal';
  var BUTTON_ID = NS + 'key-btn';

  var API_SYSTEM = '/api/system';
  var API_RESET = '/api/public/resetPassword';

  /* ---------- 样式（浅色主题，随登录页风格） ---------- */
  function injectStyle() {
    if (document.getElementById(STYLE_ID)) {
      return;
    }
    var style = document.createElement('style');
    style.id = STYLE_ID;
    style.textContent =
      '#' + BUTTON_ID +
      '{display:inline-flex;align-items:center;justify-content:center;' +
      'width:28px;height:28px;margin:0 8px 0 4px;padding:0;vertical-align:middle;' +
      'border:none;border-radius:50%;background:transparent;color:rgba(0,0,0,0.65);' +
      'cursor:pointer;transition:background-color 0.2s,color 0.2s;}' +
      '#' + BUTTON_ID + ':hover{background:rgba(0,0,0,0.06);color:#3873f6;}' +
      '#' + BUTTON_ID + ' svg{display:block;width:16px;height:16px;fill:currentColor;}' +
      '#' + MODAL_ID +
      '{position:fixed;top:0;left:0;right:0;bottom:0;z-index:9999;display:none;' +
      'align-items:center;justify-content:center;background:rgba(0,0,0,0.45);}' +
      '#' + MODAL_ID + '.' + NS + 'open{display:flex;}' +
      '#' + MODAL_ID + ' .' + NS + 'card' +
      '{box-sizing:border-box;width:360px;max-width:calc(100vw - 32px);padding:24px;' +
      'background:#fff;border-radius:8px;box-shadow:0 6px 16px rgba(0,0,0,0.12);}' +
      '#' + MODAL_ID + ' .' + NS + 'title' +
      '{margin:0 0 16px;font-size:16px;font-weight:600;color:rgba(0,0,0,0.85);text-align:center;}' +
      '#' + MODAL_ID + ' .' + NS + 'field' +
      '{margin-bottom:12px;text-align:left;}' +
      '#' + MODAL_ID + ' .' + NS + 'field-label' +
      '{display:block;margin-bottom:6px;font-size:13px;color:rgba(0,0,0,0.65);}' +
      '#' + MODAL_ID + ' .' + NS + 'input' +
      '{box-sizing:border-box;width:100%;height:32px;padding:4px 11px;' +
      'font-size:14px;color:rgba(0,0,0,0.85);background:#fff;' +
      'border:1px solid #d9d9d9;border-radius:4px;outline:none;transition:border-color 0.2s;}' +
      '#' + MODAL_ID + ' .' + NS + 'input:focus{border-color:#3873f6;}' +
      '#' + MODAL_ID + ' .' + NS + 'error' +
      '{display:none;margin-bottom:12px;font-size:13px;color:#ff4d4f;text-align:left;}' +
      '#' + MODAL_ID + ' .' + NS + 'error.' + NS + 'show{display:block;}' +
      '#' + MODAL_ID + ' .' + NS + 'actions{display:flex;justify-content:flex-end;gap:8px;margin-top:4px;}' +
      '#' + MODAL_ID + ' .' + NS + 'btn' +
      '{height:32px;padding:4px 15px;font-size:14px;border-radius:4px;cursor:pointer;' +
      'border:1px solid #d9d9d9;background:#fff;color:rgba(0,0,0,0.85);transition:all 0.2s;}' +
      '#' + MODAL_ID + ' .' + NS + 'btn:hover{border-color:#3873f6;color:#3873f6;}' +
      '#' + MODAL_ID + ' .' + NS + 'btn-primary' +
      '{background:#3873f6;border-color:#3873f6;color:#fff;}' +
      '#' + MODAL_ID + ' .' + NS + 'btn-primary:hover{background:#2b5fd6;color:#fff;}' +
      '#' + MODAL_ID + ' .' + NS + 'btn:disabled{cursor:not-allowed;opacity:0.5;}' +
      '.' + NS + 'toast' +
      '{position:fixed;top:24px;left:50%;transform:translateX(-50%);z-index:10000;' +
      'padding:8px 16px;font-size:14px;color:#fff;background:rgba(0,0,0,0.75);' +
      'border-radius:4px;box-shadow:0 2px 8px rgba(0,0,0,0.15);}';
    (document.head || document.documentElement).appendChild(style);
  }

  /* ---------- 工具 ---------- */
  function createElement(tag, attrs, styles) {
    var el = document.createElement(tag);
    if (attrs) {
      Object.keys(attrs).forEach(function (key) {
        el.setAttribute(key, attrs[key]);
      });
    }
    if (styles) {
      Object.keys(styles).forEach(function (key) {
        el.style[key] = styles[key];
      });
    }
    return el;
  }

  function showToast(text) {
    var toast = createElement('div', { className: NS + 'toast' });
    toast.textContent = text;
    document.body.appendChild(toast);
    setTimeout(function () {
      if (toast.parentNode) {
        toast.parentNode.removeChild(toast);
      }
    }, 2500);
  }

  function isBlank(value) {
    return value == null || value.trim() === '';
  }

  /* ---------- 钥匙按钮 ---------- */
  function keyIconSvg() {
    var svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
    svg.setAttribute('viewBox', '0 0 1024 1024');
    svg.setAttribute('aria-hidden', 'true');
    var path = document.createElementNS('http://www.w3.org/2000/svg', 'path');
    path.setAttribute(
      'd',
      'M542.5 32c-192.7 0-349.1 156.4-349.1 349.1 0 67.4 19.2 130.4 52.4 184.2L65.6 745.7c-7.8 7.8-12.2 18.4-12.2 29.5v151.9c0 22.6 18.3 40.9 40.9 40.9h139.5c22.6 0 40.9-18.3 40.9-40.9V843.5h83.9c22.6 0 40.9-18.3 40.9-40.9v-78.3h85.7c15.9 0 31.1-6.2 42.4-17.5l62.4-62.4c53.8 33.2 116.7 52.4 181 52.4 192.7 0 349.1-156.4 349.1-349.1C891.6 188.4 735.2 32 542.5 32zM713.2 417.6c-35.6 0-64.5-28.9-64.5-64.5s28.9-64.5 64.5-64.5 64.5 28.9 64.5 64.5-28.9 64.5-64.5 64.5z'
    );
    svg.appendChild(path);
    return svg;
  }

  function createKeyButton() {
    var button = createElement('button', {
      id: BUTTON_ID,
      type: 'button',
      title: '重置密码'
    });
    button.appendChild(keyIconSvg());
    button.addEventListener('click', function (event) {
      event.preventDefault();
      event.stopPropagation();
      openModal();
    });
    return button;
  }

  /**
   * 等待登录页 .lang 元素出现（React 异步渲染，需轮询观察）
   */
  function mountButton() {
    var langEl = document.querySelector('[data-lang="true"]');
    if (langEl) {
      langEl.appendChild(createKeyButton());
      return;
    }
    var observer = new MutationObserver(function () {
      var target = document.querySelector('[data-lang="true"]');
      if (target) {
        observer.disconnect();
        target.appendChild(createKeyButton());
      }
    });
    observer.observe(document.body, { childList: true, subtree: true });
    // 兜底：30s 内未出现登录页则放弃
    setTimeout(function () {
      observer.disconnect();
    }, 30000);
  }

  /* ---------- Modal ---------- */
  var modalEl = null;
  var inputs = {};

  function openModal() {
    if (!modalEl) {
      modalEl = buildModal();
      document.body.appendChild(modalEl);
    }
    inputs.godSecret.value = '';
    inputs.username.value = '';
    inputs.newPassword.value = '';
    hideError();
    modalEl.classList.add(NS + 'open');
    setTimeout(function () {
      inputs.godSecret.focus();
    }, 0);
  }

  function closeModal() {
    if (modalEl) {
      modalEl.classList.remove(NS + 'open');
    }
  }

  function showError(text) {
    var err = modalEl.querySelector('.' + NS + 'error');
    err.textContent = text;
    err.classList.add(NS + 'show');
  }

  function hideError() {
    var err = modalEl.querySelector('.' + NS + 'error');
    err.textContent = '';
    err.classList.remove(NS + 'show');
  }

  function buildModal() {
    var overlay = createElement('div', { id: MODAL_ID });
    var card = createElement('div', { className: NS + 'card' });
    card.addEventListener('click', function (event) {
      // 阻止冒泡，避免点击卡片内部触发遮罩关闭
      event.stopPropagation();
    });

    var title = createElement('div', { className: NS + 'title' });
    title.textContent = '重置密码';

    var field1 = buildField('外挂密码', 'godSecret', 'password');
    var field2 = buildField('账户名', 'username', 'text');
    var field3 = buildField('新密码', 'newPassword', 'password');

    var error = createElement('div', { className: NS + 'error' });

    var actions = createElement('div', { className: NS + 'actions' });
    var cancelBtn = createElement('button', { type: 'button', className: NS + 'btn' });
    cancelBtn.textContent = '取消';
    cancelBtn.addEventListener('click', function (event) {
      event.preventDefault();
      event.stopPropagation();
      closeModal();
    });
    var okBtn = createElement('button', {
      type: 'button',
      className: NS + 'btn ' + NS + 'btn-primary'
    });
    okBtn.textContent = '确认';
    okBtn.addEventListener('click', function (event) {
      event.preventDefault();
      event.stopPropagation();
      submitReset(okBtn);
    });
    actions.appendChild(cancelBtn);
    actions.appendChild(okBtn);

    card.appendChild(title);
    card.appendChild(field1);
    card.appendChild(field2);
    card.appendChild(field3);
    card.appendChild(error);
    card.appendChild(actions);
    overlay.appendChild(card);

    // 点击遮罩（非卡片区域）关闭
    overlay.addEventListener('click', function () {
      closeModal();
    });

    return overlay;
  }

  function buildField(labelText, name, type) {
    var wrap = createElement('div', { className: NS + 'field' });
    var label = createElement('label', { className: NS + 'field-label' });
    label.textContent = labelText;
    var input = createElement('input', {
      type: type,
      name: name,
      autocomplete: type === 'password' ? 'new-password' : 'username'
    });
    input.className = NS + 'input';
    inputs[name] = input;
    wrap.appendChild(label);
    wrap.appendChild(input);
    return wrap;
  }

  /* ---------- 提交 ---------- */
  function submitReset(okBtn) {
    var godSecret = inputs.godSecret.value;
    var username = inputs.username.value;
    var newPassword = inputs.newPassword.value;

    if (isBlank(godSecret) || isBlank(username) || isBlank(newPassword)) {
      showError('请填写完整信息');
      return;
    }

    hideError();
    okBtn.disabled = true;

    fetch(API_RESET, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        godSecret: godSecret,
        username: username,
        newPassword: newPassword
      })
    })
      .then(function (response) {
        return response.json();
      })
      .then(function (result) {
        okBtn.disabled = false;
        if (result && result.code === 200) {
          closeModal();
          showToast('密码已重置，请重新登录');
        } else {
          showError((result && result.message) ? result.message : '重置失败，请稍后重试');
        }
      })
      .catch(function () {
        okBtn.disabled = false;
        showError('网络异常，请稍后重试');
      });
  }

  /* ---------- 启动 ---------- */
  function init() {
    injectStyle();
    fetch(API_SYSTEM)
      .then(function (response) {
        return response.json();
      })
      .then(function (result) {
        // 仅在服务端启用外挂密码时展示入口
        if (result && result.data && result.data.isGodSecretEnabled === true) {
          mountButton();
        }
      })
      .catch(function () {
        // 网络异常时静默失败，不展示按钮
      });
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
