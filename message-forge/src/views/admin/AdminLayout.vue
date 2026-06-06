<script setup>
import { Shield, Users, FileText, BarChart3, ScrollText } from '@lucide/vue'

const sections = [
  { to: '/admin/users', label: 'Utilisateurs', icon: Users },
  { to: '/admin/templates', label: 'Templates', icon: FileText },
  { to: '/admin/stats', label: 'Statistiques', icon: BarChart3 },
  { to: '/admin/audit', label: 'Audit', icon: ScrollText },
]
</script>

<template>
  <section class="grid gap-6 lg:grid-cols-[260px_1fr]">
    <aside class="rounded-3xl border border-sky-200 bg-sky-700 p-5 text-white shadow-lg shadow-blue-900/10">
      <div class="flex items-center gap-3">
        <div class="grid h-10 w-10 place-items-center rounded-2xl bg-white/15">
          <Shield class="h-5 w-5" />
        </div>
        <div>
          <p class="text-sm uppercase tracking-[0.2em] text-sky-100">Admin</p>
          <img src="/notifyLogo.png" alt="Notify" class="mt-2 h-12 w-auto object-contain brightness-0 invert" />
        </div>
      </div>

      <nav class="mt-8 space-y-2">
        <RouterLink
          v-for="section in sections"
          :key="section.to"
          :to="section.to"
          class="flex items-center gap-3 rounded-2xl px-4 py-3 text-sm text-sky-50 transition hover:bg-white/15"
          active-class="bg-white text-sky-800 shadow-sm"
        >
          <component :is="section.icon" class="h-4 w-4" />
          {{ section.label }}
        </RouterLink>
      </nav>
    </aside>

    <main class="min-w-0">
      <RouterView />
    </main>
  </section>
</template>
