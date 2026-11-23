// The tap counter for the test-2 ad page /android-t2/ (BRD D196). It answers one thing, for every request alike: 302 to
// the Google Play link below. It reads nothing from the request (no method, IP, user agent, Referer, cookie), writes
// nothing (no storage, no log) and calls nobody. The count is Cloudflare's own Pages "Functions metrics" Requests for
// this project, so this must stay the project's only Function. website/build.py stops the build unless every line of
// this file other than comment lines is exactly the code it expects.
const PLAY = 'https://play.google.com/store/apps/details?id=com.migrely.app&utm_source=instagram&utm_medium=paid_social&utm_campaign=ig-ads-test2&referrer=utm_source%3Dinstagram%26utm_medium%3Dpaid_social%26utm_campaign%3Dig-ads-test2';

export function onRequest() {
  return new Response(null, {
    status: 302,
    headers: {
      Location: PLAY,
      'Cache-Control': 'no-store',
      'Referrer-Policy': 'no-referrer',
      'X-Robots-Tag': 'noindex',
    },
  });
}
