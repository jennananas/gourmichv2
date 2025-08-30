import{a as re}from"./chunk-TD6ODWFL.js";import{e as E,f as oe}from"./chunk-HSIN7PCI.js";import{c as G,e as J,g as W,h as X,l as Y,m as Z,sa as ee,ta as y,ua as te,va as ie,ya as ne}from"./chunk-C633TUPX.js";import{$ as l,Db as Q,Ma as M,N as b,Na as U,Nb as K,O as V,Qa as F,S as C,Sa as m,T as O,Ub as q,Ya as h,Z as A,Za as a,_ as p,_a as j,ab as P,ba as x,bb as g,db as _,eb as u,fb as H,gb as T,ha as I,hb as w,jb as f,kb as d,lb as c,mb as z,nb as D,pb as $,rb as k,sb as R,vb as B,wb as L,xa as S,xb as N,za as s}from"./chunk-2KLC34RG.js";var pe=["removeicon"],le=["*"];function me(e,r){if(e&1){let t=f();_(0,"img",4),d("error",function(n){p(t);let o=c();return l(o.imageError(n))}),u()}if(e&2){let t=c();a("src",t.image,S)("alt",t.alt)}}function he(e,r){if(e&1&&H(0,"span",6),e&2){let t=c(2);g(t.icon),a("ngClass","p-chip-icon"),h("data-pc-section","icon")}}function _e(e,r){if(e&1&&m(0,he,1,4,"span",5),e&2){let t=c();a("ngIf",t.icon)}}function ue(e,r){if(e&1&&(_(0,"div",7),L(1),u()),e&2){let t=c();h("data-pc-section","label"),s(),N(t.label)}}function ge(e,r){if(e&1){let t=f();_(0,"span",11),d("click",function(n){p(t);let o=c(3);return l(o.close(n))})("keydown",function(n){p(t);let o=c(3);return l(o.onKeydown(n))}),u()}if(e&2){let t=c(3);g(t.removeIcon),a("ngClass","p-chip-remove-icon"),h("data-pc-section","removeicon")("aria-label",t.removeAriaLabel)}}function fe(e,r){if(e&1){let t=f();_(0,"TimesCircleIcon",12),d("click",function(n){p(t);let o=c(3);return l(o.close(n))})("keydown",function(n){p(t);let o=c(3);return l(o.onKeydown(n))}),u()}if(e&2){let t=c(3);g("p-chip-remove-icon"),h("data-pc-section","removeicon")("aria-label",t.removeAriaLabel)}}function de(e,r){if(e&1&&(T(0),m(1,ge,1,5,"span",9)(2,fe,1,4,"TimesCircleIcon",10),w()),e&2){let t=c(2);s(),a("ngIf",t.removeIcon),s(),a("ngIf",!t.removeIcon)}}function ve(e,r){}function be(e,r){e&1&&m(0,ve,0,0,"ng-template")}function ye(e,r){if(e&1){let t=f();_(0,"span",13),d("click",function(n){p(t);let o=c(2);return l(o.close(n))})("keydown",function(n){p(t);let o=c(2);return l(o.onKeydown(n))}),m(1,be,1,0,null,14),u()}if(e&2){let t=c(2);h("data-pc-section","removeicon")("aria-label",t.removeAriaLabel),s(),a("ngTemplateOutlet",t.removeIconTemplate||t._removeIconTemplate)}}function Ce(e,r){if(e&1&&(T(0),m(1,de,3,2,"ng-container",3)(2,ye,2,3,"span",8),w()),e&2){let t=c();s(),a("ngIf",!t.removeIconTemplate&&!t._removeIconTemplate),s(),a("ngIf",t.removeIconTemplate||t._removeIconTemplate)}}var xe=({dt:e})=>`
.p-chip {
    display: inline-flex;
    align-items: center;
    background: ${e("chip.background")};
    color: ${e("chip.color")};
    border-radius: ${e("chip.border.radius")};
    padding: ${e("chip.padding.y")} ${e("chip.padding.x")};
    gap: ${e("chip.gap")};
}

.p-chip-icon {
    color: ${e("chip.icon.color")};
    font-size: ${e("chip.icon.font.size")};
    width: ${e("chip.icon.size")};
    height: ${e("chip.icon.size")};
}

.p-chip-image {
    border-radius: 50%;
    width: ${e("chip.image.width")};
    height: ${e("chip.image.height")};
    margin-left: calc(-1 * ${e("chip.padding.y")});
}

.p-chip:has(.p-chip-remove-icon) {
    padding-inline-end: ${e("chip.padding.y")};
}

.p-chip:has(.p-chip-image) {
    padding-top: calc(${e("chip.padding.y")} / 2);
    padding-bottom: calc(${e("chip.padding.y")} / 2);
}

.p-chip-remove-icon {
    cursor: pointer;
    font-size: ${e("chip.remove.icon.font.size")};
    width: ${e("chip.remove.icon.size")};
    height: ${e("chip.remove.icon.size")};
    color: ${e("chip.remove.icon.color")};
    border-radius: 50%;
    transition: outline-color ${e("chip.transition.duration")}, box-shadow ${e("chip.transition.duration")};
    outline-color: transparent;
}

.p-chip-remove-icon:focus-visible {
    box-shadow: ${e("chip.remove.icon.focus.ring.shadow")};
    outline: ${e("chip.remove.icon.focus.ring.width")} ${e("chip.remove.icon.focus.ring.style")} ${e("chip.remove.icon.focus.ring.color")};
    outline-offset: ${e("chip.remove.icon.focus.ring.offset")};
}
`,Ie={root:"p-chip p-component",image:"p-chip-image",icon:"p-chip-icon",label:"p-chip-label",removeIcon:"p-chip-remove-icon"},ce=(()=>{class e extends ie{name="chip";theme=xe;classes=Ie;static \u0275fac=(()=>{let t;return function(n){return(t||(t=x(e)))(n||e)}})();static \u0275prov=b({token:e,factory:e.\u0275fac})}return e})();var Te=(()=>{class e extends ne{label;icon;image;alt;style;styleClass;removable=!1;removeIcon;onRemove=new I;onImageError=new I;visible=!0;get removeAriaLabel(){return this.config.getTranslation(te.ARIA).removeLabel}get chipProps(){return this._chipProps}set chipProps(t){this._chipProps=t,t&&typeof t=="object"&&Object.entries(t).forEach(([i,n])=>this[`_${i}`]!==n&&(this[`_${i}`]=n))}_chipProps;_componentStyle=O(ce);removeIconTemplate;templates;_removeIconTemplate;ngAfterContentInit(){this.templates.forEach(t=>{switch(t.getType()){case"removeicon":this._removeIconTemplate=t.template;break;default:this._removeIconTemplate=t.template;break}})}ngOnChanges(t){if(super.ngOnChanges(t),t.chipProps&&t.chipProps.currentValue){let{currentValue:i}=t.chipProps;i.label!==void 0&&(this.label=i.label),i.icon!==void 0&&(this.icon=i.icon),i.image!==void 0&&(this.image=i.image),i.alt!==void 0&&(this.alt=i.alt),i.style!==void 0&&(this.style=i.style),i.styleClass!==void 0&&(this.styleClass=i.styleClass),i.removable!==void 0&&(this.removable=i.removable),i.removeIcon!==void 0&&(this.removeIcon=i.removeIcon)}}containerClass(){let t="p-chip p-component";return this.styleClass&&(t+=` ${this.styleClass}`),t}close(t){this.visible=!1,this.onRemove.emit(t)}onKeydown(t){(t.key==="Enter"||t.key==="Backspace")&&this.close(t)}imageError(t){this.onImageError.emit(t)}static \u0275fac=(()=>{let t;return function(n){return(t||(t=x(e)))(n||e)}})();static \u0275cmp=M({type:e,selectors:[["p-chip"]],contentQueries:function(i,n,o){if(i&1&&($(o,pe,4),$(o,ee,4)),i&2){let v;k(v=R())&&(n.removeIconTemplate=v.first),k(v=R())&&(n.templates=v)}},hostVars:9,hostBindings:function(i,n){i&2&&(h("data-pc-name","chip")("aria-label",n.label)("data-pc-section","root"),P(n.style),g(n.containerClass()),j("display",!n.visible&&"none"))},inputs:{label:"label",icon:"icon",image:"image",alt:"alt",style:"style",styleClass:"styleClass",removable:[2,"removable","removable",q],removeIcon:"removeIcon",chipProps:"chipProps"},outputs:{onRemove:"onRemove",onImageError:"onImageError"},features:[Q([ce]),F,A],ngContentSelectors:le,decls:6,vars:4,consts:[["iconTemplate",""],["class","p-chip-image",3,"src","alt","error",4,"ngIf","ngIfElse"],["class","p-chip-label",4,"ngIf"],[4,"ngIf"],[1,"p-chip-image",3,"error","src","alt"],[3,"class","ngClass",4,"ngIf"],[3,"ngClass"],[1,"p-chip-label"],["tabindex","0","class","p-chip-remove-icon","role","button",3,"click","keydown",4,"ngIf"],["tabindex","0","role","button",3,"class","ngClass","click","keydown",4,"ngIf"],["tabindex","0","role","button",3,"class","click","keydown",4,"ngIf"],["tabindex","0","role","button",3,"click","keydown","ngClass"],["tabindex","0","role","button",3,"click","keydown"],["tabindex","0","role","button",1,"p-chip-remove-icon",3,"click","keydown"],[4,"ngTemplateOutlet"]],template:function(i,n){if(i&1&&(z(),D(0),m(1,me,1,2,"img",1)(2,_e,1,1,"ng-template",null,0,K)(4,ue,2,2,"div",2)(5,Ce,3,2,"ng-container",3)),i&2){let o=B(3);s(),a("ngIf",n.image)("ngIfElse",o),s(3),a("ngIf",n.label),s(),a("ngIf",n.removable)}},dependencies:[X,G,J,W,re,y],encapsulation:2,changeDetection:0})}return e})(),De=(()=>{class e{static \u0275fac=function(i){return new(i||e)};static \u0275mod=U({type:e});static \u0275inj=V({imports:[Te,y,y]})}return e})();var ae=class e{constructor(r,t){this.http=r;this.authService=t}recipesUrl=`${E.apiUrl}recipes`;favoritesUrl=`${E.apiUrl}favorites`;getAuthHeaders(){let r=this.authService.getToken();return new Y({Authorization:`Bearer ${r}`})}getAllRecipes(){return this.http.get(this.recipesUrl)}getRecipeById(r){return this.http.get(`${this.recipesUrl}/by-id/${r}`)}createRecipe(r){return this.http.post(`${this.recipesUrl}`,r)}deleteRecipe(r){return this.http.delete(`${this.recipesUrl}/by-id/${r}`)}updateRecipe(r,t){return this.http.put(`${this.recipesUrl}/by-id/${r}`,t)}getFavorites(){return this.http.get(this.favoritesUrl,{headers:this.getAuthHeaders()})}toggleFavorite(r){return this.http.post(`${this.favoritesUrl}/toggle?recipeId=${r}`,{},{headers:this.getAuthHeaders(),responseType:"text"})}isFavorite(r){return this.http.get(`${this.favoritesUrl}/is-favorite/${r}`,{headers:this.getAuthHeaders()})}getLatestRecipes(r=3){return this.http.get(`${this.recipesUrl}/latest?n=${r}`)}static \u0275fac=function(t){return new(t||e)(C(Z),C(oe))};static \u0275prov=b({token:e,factory:e.\u0275fac,providedIn:"root"})};export{Te as a,De as b,ae as c};
