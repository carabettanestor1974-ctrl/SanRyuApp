from pathlib import Path
import re

html = Path("app/src/main/assets/index.html")
s = html.read_text(encoding="utf-8")

if "const na=$('newAnnouncementBtn'); if(na) na.classList.add('hidden');" not in s:
    old = """function setupStudentUI(){
 setAdminActionButtons(false);
 const ai=$('authorityAdminIntro'); if(ai) ai.classList.add('hidden');"""
    new = """function setupStudentUI(){
 setAdminActionButtons(false);
 const na=$('newAnnouncementBtn'); if(na) na.classList.add('hidden');
 const ai=$('authorityAdminIntro'); if(ai) ai.classList.add('hidden');"""
    if old in s:
        s = s.replace(old, new, 1)
    else:
        raise SystemExit("No se encontró setupStudentUI ni el ocultamiento ya aplicado")

new_open = """async function openMaterial(path){
 if(!path){showModal('Archivo','<div class="empty">El material todavía no tiene un archivo asociado.</div>');return}
 try{
   let target=path;
   if(!/^https?:\\/\\//i.test(path)){
     const signed=await api('/storage/v1/object/sign/'+CONFIG.materialBucket+'/'+path,{method:'POST',body:JSON.stringify({expiresIn:3600})});
     const signedPath=signed?.signedURL||signed?.signedUrl||'';
     if(!signedPath)throw new Error('No se pudo generar el acceso al PDF.');
     target=/^https?:\\/\\//i.test(signedPath)?signedPath:CONFIG.url+signedPath;
   }
   location.href=target;
 }catch(e){
   showModal('No se pudo abrir',`<div class="status error">${esc(e.message)}</div>`);
 }
}"""

pattern = r"async function openMaterial\(path\)\{.*?\nfunction openMaterialModal\(\)"
if not re.search(pattern, s, re.S):
    raise SystemExit("No se encontró openMaterial")
s = re.sub(pattern, new_open + "\nfunction openMaterialModal(", s, count=1, flags=re.S)
html.write_text(s, encoding="utf-8")

gradle = Path("app/build.gradle.kts")
g = gradle.read_text(encoding="utf-8")
g = g.replace("versionCode = 2", "versionCode = 3", 1)
gradle.write_text(g, encoding="utf-8")

main = Path("app/src/main/java/com/shanryu/benitojuarez/MainActivity.kt")
m = main.read_text(encoding="utf-8")
old = """            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean =
                false"""
new = """            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val uri = request.url
                if (uri.scheme == "http" || uri.scheme == "https") {
                    val isAppAsset = uri.host == "appassets.androidplatform.net"
                    if (!isAppAsset) {
                        startActivity(Intent(Intent.ACTION_VIEW, uri))
                        return true
                    }
                }
                return false
            }"""
if old in m:
    m = m.replace(old, new, 1)
main.write_text(m, encoding="utf-8")
