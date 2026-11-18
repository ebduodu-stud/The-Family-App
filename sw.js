const CACHE='family-morning-worship-v1';
const CORE=['./','./index.html','./manifest.webmanifest','./icon.svg'];
const SCHEDULE=[
'./Fam%20App/app/src/main/assets/schedule/Schedule_Nov2026-Jan2027.pdf',
'./Fam%20App/app/src/main/assets/schedule/Schedule_Feb-Apr2027.pdf',
'./Fam%20App/app/src/main/assets/schedule/Schedule_May-Jun2027.pdf'
];
self.addEventListener('install',event=>event.waitUntil(caches.open(CACHE).then(cache=>cache.addAll(CORE)).then(()=>self.skipWaiting())));
self.addEventListener('activate',event=>event.waitUntil(caches.keys().then(keys=>Promise.all(keys.filter(k=>k!==CACHE).map(k=>caches.delete(k)))).then(()=>self.clients.claim())));
self.addEventListener('fetch',event=>{
 const request=event.request;
 if(request.method!=='GET'||new URL(request.url).origin!==self.location.origin)return;
 event.respondWith(caches.match(request).then(cached=>{
  const fresh=fetch(request).then(response=>{if(response.ok){const copy=response.clone();caches.open(CACHE).then(cache=>cache.put(request,copy))}return response}).catch(()=>cached);
  return cached||fresh;
 }));
});