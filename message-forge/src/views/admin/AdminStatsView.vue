<script setup>
import { computed, onMounted } from 'vue'
import { useAdminStore } from '../../stores/admin'

const adminStore = useAdminStore()
const stats = computed(() => adminStore.stats)

onMounted(() => {
  adminStore.fetchStats()
})
</script>

<template>
  <section class="rounded-3xl border border-sky-200 bg-sky-100 p-6 shadow-lg shadow-blue-900/10">
    <h2 class="text-2xl font-semibold text-slate-950">Statistiques globales</h2>
    <p class="mt-2 text-sm text-slate-600">Vue d'ensemble de l'utilisation de la plateforme.</p>
    
    <div v-if="adminStore.isLoading" class="mt-6 text-sm text-slate-500">
      Chargement des statistiques...
    </div>
    
    <div v-else-if="adminStore.error" class="mt-6 rounded-2xl bg-rose-50 px-4 py-3 text-sm text-rose-700">
      {{ adminStore.error }}
    </div>

    <div v-else-if="stats" class="mt-6 space-y-6">
      <div class="grid gap-4 sm:grid-cols-2">
        <div class="rounded-3xl border border-sky-100 bg-white p-5 shadow-sm">
          <p class="text-sm font-medium text-slate-500">Total Utilisateurs</p>
          <p class="mt-2 text-4xl font-semibold text-sky-900">{{ stats.totalUsers }}</p>
        </div>
        <div class="rounded-3xl border border-sky-100 bg-white p-5 shadow-sm">
          <p class="text-sm font-medium text-slate-500">Total Messages Créés</p>
          <p class="mt-2 text-4xl font-semibold text-sky-900">{{ stats.totalMessages }}</p>
        </div>
      </div>

      <h3 class="text-lg font-semibold text-slate-900">Détails par canal</h3>
      <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        <div v-for="chan in stats.channelStats" :key="chan.channel" class="rounded-3xl border border-sky-100 bg-white p-5 shadow-sm">
          <p class="text-sm font-bold uppercase tracking-wider text-sky-800">{{ chan.channel }}</p>
          <div class="mt-4 space-y-2">
            <div class="flex justify-between text-sm">
              <span class="text-slate-500">Envoyés</span>
              <span class="font-semibold text-emerald-600">{{ chan.sent }}</span>
            </div>
            <div class="flex justify-between text-sm">
              <span class="text-slate-500">En attente</span>
              <span class="font-semibold text-amber-600">{{ chan.pending }}</span>
            </div>
            <div class="flex justify-between text-sm">
              <span class="text-slate-500">Échoués</span>
              <span class="font-semibold text-rose-600">{{ chan.failed }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
