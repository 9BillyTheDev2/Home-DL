// Synthetic page data only. Does not start a browser or access the network.
const vm = require('node:vm');
const fs = require('node:fs');
const assert = require('node:assert/strict');
const source = fs.readFileSync('app/src/main/assets/browser/instagram.js', 'utf8')
  .replace('__EXPECTED_CODE__', '"ABC"').replace('__EXPECTED_ID__', '"66"');
const photo = {media_type: 1, image_versions2: {candidates: [{url: 'https://test.cdninstagram.com/photo.jpg', width: 1200, height: 800}]}};
function run(data) {
  const context = { URL, TextDecoder, Uint8Array, WeakSet, location: {href: 'https://www.instagram.com/p/ABC/', origin: 'https://www.instagram.com'},
    document: {querySelectorAll: () => [{textContent: JSON.stringify(data)}], querySelector: () => null} };
  context.window = context;
  vm.runInNewContext(source, context, {timeout: 1000});
  return JSON.parse(context.__homeDlBrowser.snapshot());
}
let result = run({data: {item: {code: 'ABC', pk: '66', ...photo}}});
assert.equal(result.product.code, 'ABC');
assert.equal(result.product.image_versions2.candidates[0].width, 1200);
assert.equal(run({item: {code: 'OTHER', ...photo}}).product, null);
assert.equal(run({item: {code: 'ABC', media_type: 2, image_versions2: photo.image_versions2}}).product, null);
assert.equal(run({item: {code: 'ABC', carousel_media: [photo, {media_type: 2}]}}).product, null);
assert.equal(run({item: {code: 'ABC', ...photo, image_versions2: {candidates: [{url:'https://evil.invalid/a.jpg'}]}}}).product, null);
result = run({data: {code:'ABC', carousel_media: [photo, photo], author: 'PRIVATE_AUTHOR'}});
assert.equal(result.product.carousel_media.length, 2);
assert.equal(JSON.stringify(result).includes('PRIVATE_AUTHOR'), false);
result = run({data:{shortcode:'ABC', is_video:true, video_url:'https://test.cdninstagram.com/video.mp4', display_url:'https://test.cdninstagram.com/cover.jpg', dimensions:{width:720,height:1280}}});
assert.equal(result.product.media_type, 2);
assert.equal(result.product.video_versions[0].height, 1280);
console.log('Browser capture: 7 synthetic scenarios passed');
(async () => {
  const payload = {data:{item:{code:'ABC',pk:'66',...photo}}, private:'PRIVATE_RESPONSE'};
  const context = { URL, TextDecoder, Uint8Array, WeakSet,
    location:{href:'https://www.instagram.com/p/ABC/',origin:'https://www.instagram.com'},
    document:{querySelectorAll:()=>[],querySelector:()=>null},
    fetch:()=> {
      const response = new Response(JSON.stringify(payload), {status:200});
      Object.defineProperty(response, 'url', {value:'https://www.instagram.com/api/graphql'});
      return Promise.resolve(response);
    }
  };
  context.window = context;
  vm.runInNewContext(source, context, {timeout:1000});
  const response = await context.fetch('/api/graphql');
  assert.deepEqual(await response.json(), payload); // observer must not consume the page's response
  for (let n=0;n<20;n++) {
    const result = JSON.parse(context.__homeDlBrowser.snapshot());
    if (result.product) {
      assert.equal(result.product.code,'ABC');
      assert.equal(result.stats.responses,1);
      assert.equal(JSON.stringify(result).includes('PRIVATE_RESPONSE'), false);
      console.log('Browser capture: fetch observation preserves original response');
      return;
    }
    await new Promise(resolve=>setTimeout(resolve,10));
  }
  throw new Error('Response observer did not capture the matching product');
})().catch(error=>{ console.error(error);process.exitCode=1; });
