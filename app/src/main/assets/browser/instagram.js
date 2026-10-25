(() => {
  'use strict';
  if (window.__homeDlBrowser) return;
  const expected = __EXPECTED_CODE__;
  const expectedId = __EXPECTED_ID__;
  const stats = { responses: 0, parsed: 0, scripts: 0, matched: 0 };
  let product = null;
  const seenScripts = new WeakSet();
  const validUrl = value => {
    try {
      const u = new URL(value);
      return u.protocol === 'https:' && !u.username && !u.password && (!u.port || u.port === '443') &&
        (u.hostname.endsWith('.cdninstagram.com') || u.hostname.endsWith('.fbcdn.net')) && value.length <= 4096;
    } catch (_) { return false; }
  };
  const variants = list => Array.isArray(list) ? list.filter(x => x && validUrl(x.url))
    .map(x => ({ url: x.url, width: Number(x.width) || 0, height: Number(x.height) || 0 }))
    .sort((a, b) => b.width * b.height - a.width * a.height).slice(0, 4) : [];
  const cleanItem = x => {
    if (!x || typeof x !== 'object') return null;
    const old = typeof x.is_video === 'boolean';
    const video = x.media_type === 2 || x.is_video === true || (Array.isArray(x.video_versions) && x.video_versions.length > 0);
    let photos = variants(x.image_versions2 && x.image_versions2.candidates);
    let videos = variants(x.video_versions);
    if (old && !photos.length && validUrl(x.display_url)) photos = [{url: x.display_url, width: x.dimensions?.width || 0, height: x.dimensions?.height || 0}];
    if (old && video && !videos.length && validUrl(x.video_url)) videos = [{url: x.video_url, width: x.dimensions?.width || 0, height: x.dimensions?.height || 0}];
    if ((video && !videos.length) || (!video && !photos.length)) return null;
    return {media_type: video ? 2 : 1, image_versions2: {candidates: photos}, ...(video ? {video_versions: videos} : {})};
  };
  function inspect(root) {
    const queue = [root]; let visited = 0;
    while (queue.length && visited++ < 20000 && !product) {
      const x = queue.pop();
      if (!x || typeof x !== 'object') continue;
      const code = x.code || x.shortcode;
      const id = String(x.pk || x.id || '').split('_')[0];
      if (code === expected || (!code && id === expectedId)) {
        const children = x.carousel_media || x.edge_sidecar_to_children?.edges?.map(e => e.node);
        const parts = children || [x];
        if (Array.isArray(parts) && parts.length >= 1 && parts.length <= 50) {
          const cleaned = parts.map(cleanItem);
          // Reject incomplete carousels and video covers instead of silently losing media.
          if (cleaned.every(Boolean)) {
            const candidate = {code: expected, pk: expectedId, ...(children ? {carousel_media: cleaned} : cleaned[0])};
            if (JSON.stringify(candidate).length <= 180000) { product = candidate; stats.matched++; }
          }
        }
      }
      if (Array.isArray(x)) {
        for (const v of x.slice(0, 2000)) if (queue.length < 20000) queue.push(v);
      } else {
        for (const key of Object.keys(x).slice(0, 2000)) if (x[key] && typeof x[key] === 'object' && queue.length < 20000) queue.push(x[key]);
      }
    }
  }
  function accept(text) {
    if (typeof text !== 'string' || text.length > 4194304) return;
    const lines = text.replace(/^for\s*\(;;\);\s*/, '').split('\n').slice(0, 25);
    try { inspect(JSON.parse(text)); stats.parsed++; return; } catch (_) {}
    for (const line of lines) { try { inspect(JSON.parse(line)); stats.parsed++; } catch (_) {} }
  }
  const relevant = url => {
    try { const u = new URL(url, location.href); return u.origin === location.origin && (u.pathname.includes('graphql') || u.pathname.startsWith('/api/v1/')); }
    catch (_) { return false; }
  };
  if (window.fetch) {
    const original = window.fetch;
    window.fetch = function(...args) {
      const result = original.apply(this, args);
      result.then(async response => {
        if (!relevant(response.url) || response.status !== 200 || product) return;
        stats.responses++;
        try {
          const clone = response.clone(), reader = clone.body?.getReader();
          if (!reader) return;
          const chunks = []; let size = 0;
          while (true) {
            const next = await reader.read(); if (next.done) break;
            size += next.value.length;
            if (size > 4194304) { reader.cancel().catch(() => {}); return; }
            chunks.push(next.value);
          }
          const bytes = new Uint8Array(size); let offset = 0;
          for (const chunk of chunks) { bytes.set(chunk, offset); offset += chunk.length; }
          accept(new TextDecoder().decode(bytes));
        } catch (_) {}
      }).catch(() => {});
      return result;
    };
  }
  if (window.XMLHttpRequest) {
    const originalOpen = XMLHttpRequest.prototype.open;
    XMLHttpRequest.prototype.open = function(...args) {
      if (relevant(args[1])) this.addEventListener('load', () => {
        if (this.status !== 200 || product) return;
        stats.responses++;
        try { if (this.responseType === 'json') inspect(this.response); else accept(this.responseText); } catch (_) {}
      });
      return originalOpen.apply(this, args);
    };
  }
  window.__homeDlBrowser = { snapshot() {
    let total = 0;
    for (const script of document.querySelectorAll('script[type="application/json"]')) {
      if (seenScripts.has(script)) continue;
      const text = script.textContent || ''; total += text.length;
      if (total > 6291456) break;
      seenScripts.add(script); stats.scripts++; accept(text);
    }
    return JSON.stringify({product, stats, login: !!document.querySelector('input[type="password"]')});
  }};
})();
