import { Component, Input } from '@angular/core';

export const ESCUDOS = [
 { codigo:'VERDE', nome:'Verde', cor:'#63B84D' },
 { codigo:'AZUL', nome:'Azul', cor:'#4185D7' },
 { codigo:'VERMELHO', nome:'Vermelho', cor:'#D75050' },
 { codigo:'AMARELO', nome:'Amarelo', cor:'#E5BD43' },
 { codigo:'ROXO', nome:'Roxo', cor:'#A570D4' },
 { codigo:'LARANJA', nome:'Laranja', cor:'#E78A3D' }
];

export const CORES_ESCUDO = [
 {nome:'Verde-lima',valor:'#7ED957'}, {nome:'Verde-escuro',valor:'#228B55'},
 {nome:'Azul',valor:'#3478F6'}, {nome:'Azul-marinho',valor:'#1C3978'},
 {nome:'Azul-claro',valor:'#55C5F4'}, {nome:'Vermelho',valor:'#E53935'},
 {nome:'Vinho',valor:'#8C2645'}, {nome:'Amarelo',valor:'#F6CE3E'},
 {nome:'Dourado',valor:'#C99B2D'}, {nome:'Laranja',valor:'#F28C32'},
 {nome:'Roxo',valor:'#9256DB'}, {nome:'Lilás',valor:'#C39AEF'},
 {nome:'Rosa',valor:'#EF75B0'}, {nome:'Preto',valor:'#222222'},
 {nome:'Branco',valor:'#F5F5F5'}, {nome:'Cinza',valor:'#89939A'}
];
@Component({
 selector:'app-escudo', standalone:false,
 template:'<span aria-hidden="true" class="escudo" [style.background]="cor"><span [style.background]="contraste"></span></span>',
 styles:[`
 :host { display:inline-flex; vertical-align:middle; margin-right:6px; flex-shrink:0; filter:drop-shadow(0 0 1px #EAF4EE); }
 .escudo { display:inline-flex; justify-content:center; width:22px; height:26px; clip-path:polygon(0 0,100% 0,92% 68%,50% 100%,8% 68%); }
 .escudo span { width:5px; height:22px; background:rgba(255,255,255,.75); }
 `]
})
export class EscudoComponent {
 @Input() codigo: string | null | undefined = 'VERDE';
 @Input() corEscudo: string | null | undefined = '#7ED957';
 get cor(): string { return CORES_ESCUDO.find(c => c.valor === this.corEscudo)?.valor ?? '#7ED957'; }
 get contraste(): string {
  const componentes = [1,3,5].map(i => {
   const valor = parseInt(this.cor.slice(i,i+2),16)/255;
   return valor <= 0.04045 ? valor/12.92 : Math.pow((valor+0.055)/1.055,2.4);
  });
  const luminancia = componentes[0]*0.2126 + componentes[1]*0.7152 + componentes[2]*0.0722;
  return luminancia > 0.179 ? '#111A17' : '#FFFFFF';
 }
}
