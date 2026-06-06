<template>
  <section class="rounded-3xl border border-sky-200 bg-sky-100 p-6 shadow-lg shadow-blue-900/10">
    <h2 class="text-2xl font-semibold text-slate-950">Statistiques par canal</h2>
    <p class="mt-2 text-sm text-slate-600">Route prete pour consulter les indicateurs Email, SMS, Slack, X et autres canaux.</p>
    <div class="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
      <div v-for="channel in ['Email', 'SMS', 'Slack', 'X']" :key="channel" class="rounded-3xl border border-sky-100 bg-sky-50 p-5">
        <p class="text-sm text-slate-600">{{ channel }}</p>
        <p class="mt-2 text-3xl font-semibold text-sky-800">0</p>
      </div>
    </div>
  </section>
</template>
