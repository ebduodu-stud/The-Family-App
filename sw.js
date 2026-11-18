const CACHE='family-morning-worship-v2';
const CORE=['./','./index.html','./manifest.webmanifest','./icon.svg'];
const SCHEDULE=[
'./Fam%20App/app/src/main/assets/schedule/Schedule_Nov2026-Jan2027.pdf',
'./Fam%20App/app/src/main/assets/schedule/Schedule_Feb-Apr2027.pdf',
'./Fam%20App/app/src/main/assets/schedule/Schedule_May-Jun2027.pdf'
];
self.addEventListener('install',event=>event.waitUntil(caches.open(CACHE).then(cache=>cache.addAll(CORE)).then(()=>self.skipWaiting())));
self.addEventListener('activate',event=>event.waitUntil(caches.keys().then(keys=>Promise.all(keys.filter(k=>k.startsWith('family-morning-worship-')&&k!==CACHE).map(k=>caches.delete(k)))).then(()=>self.clients.claim())));
self.addEventListener('fetch',event=>{
 const request=event.request;
 if(request.method!=='GET'||new URL(request.url).origin!==self.location.origin)return;
 event.respondWith((async()=>{
  const cache=await caches.open(CACHE);
  try{
   const response=await fetch(request,{cache:'no-store'});
   if(response&&response.ok) await cache.put(request,response.clone());
   return response;
  }catch(error){
   const cached=await cache.match(request);
   if(cached)return cached;
   if(request.mode==='navigate') return cache.match('./index.html');
   throw error;
  }
 })());
});
