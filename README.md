#   p a r a l l e l - b l a c k j a c k  
  
 # # #   P r o g r a m   A r g u m e n t s  
  
 c l i e n t :   ` [ n u m b e r   o f   p l a y e r s ] `   ( e . g .   2 )  
  
 s e r v e r :   ` [ i p   a d d r e s s ]   [ p o r t   n u m b e r ] `   ( 1 2 7 . 0 . 0 . 1   6 1 0 1 3 )  
  
 # # #   I n t e n d e d   P r o g r a m   F l o w  
  
 -   s e r v e r   o p e n s  
  
 -   c l i e n t   c o n n e c t s  
  
 -   w e l c o m e   m e s s a g e   i s   s e n t   t o   c l i e n t  
  
 -   c l i e n t   g o e s   t o   s l e e p  
  
 -   s e r v e r   w a i t s   f o r   s e c o n d   c l i e n t  
  
 -   s e c o n d   c l i e n t   c o n n e c t s  
  
 -   w e l c o m e   m e s s a g e   i s   s e n t   t o   c l i e n t  
  
 -   c l i e n t   g o e s   t o   s l e e p  
  
 * * T h e   G a m e   B e g i n s ! * *  
  
 -   d e a l e r   g e t s   a   c a r d  
  
 -   p l a y e r 1   g e t s   a   c a r d  
  
 -   p l a y e r 2   g e t s   a   c a r d  
  
 -   d e a l e r   g e t s   a   c a r d  
  
 -   p l a y e r 1   g e t s   a   c a r d  
  
 -   p l a y e r 2   g e t s   a   c a r d  
  
 * * A l l   p l a y e r s   a r e   t o l d   t h e   s t a t e   o f   p l a y * *  
  
 -   p l a y e r   2   i s   t o l d   t o   w a i t  
  
 -   p l a y e r   1 ' s   t h r e a d   w a k e s   u p  
  
 -   p l a y e r   1   p l a y s   a   r o u n d   u n t i l   t h e y   s t o p ,   b l a c k j a c k ,   o r   b u s t  
  
 -   t h e i r   * * r o u n d * *   i s   o v e r  
  
 -   t h e y   g o   t o   s l e e p   u n t i l   t h e   e n d   o f   t h e   g a m e  
  
 -   a l l   t h r e a d s   a r e   n o t i f i e d  
  
 -   p l a y e r   2 ' s   t h r e a d   w a k e s   u p  
  
 -   p l a y e r 2   g e t s   t o   d o   t h e i r   t h i n g  
  
 -   t h e i r   * * r o u n d * *   i s   o v e r  
  
 -   t h e   * * g a m e * *   i s   f l a g g e d   a s   o v e r  
  
 -   t h e y   g o   t o   s l e e p   u n t i l   t h e   e n d   o f   t h e   g a m e  
  
 * * T h e   G a m e   i s   O v e r ! * *  
  
 -   t h e   g a m e   i s   r e c o g n i z e d   a s   o v e r   * * b y   m a i n * *  
  
 -   m a i n   t a l l i e s   u p   a l l   t h e   g o o d   s t u f f  
  
 -   * * a l l   t h r e a d s * *   p r i n t   o u t   t h e   r e s u l t s  
  
 -   a l l   c l i e n t s   d i s c o n n e c t  
  
 -   s e r v e r   s h u t s   d o w n  
  
 * * E v e r y o n e   g e t s   c a k e . * * 