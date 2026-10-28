<template>
  <AppLayout>
    <router-view v-slot="{ Component }">
      <transition name="fade" mode="out-in">
        <component :is="Component" :key="$route.fullPath" />
      </transition>
    </router-view>
  </AppLayout>
</template>

<script setup>
import AppLayout from '@/layout/AppLayout.vue'
</script>
