<template>
  <router-view />
</template>

<!--
  样式已全部迁到 src/styles/（由 main.js 引入）。
  这里刻意不再放 <style> 块，避免出现「两个全局样式入口」。
  原先这里有一条 html,body,#app{height:100%}，它会和 Element Plus 的
  useLockscreen 打架、导致长页面打开弹窗时跳到顶部，修正见 styles/base.css。
-->
