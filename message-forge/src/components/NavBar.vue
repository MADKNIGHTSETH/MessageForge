<script setup>
import { computed } from "vue";
import { useRoute } from "vue-router";
import {
  Clock3,
  FileText,
  LayoutDashboard,
  LogIn,
  PenLine,
  Search,
  ShieldCheck,
  UserPlus,
} from "@lucide/vue";
import { useAuthStore } from "@/stores/auth";

const route = useRoute();
const authStore = useAuthStore();

const isAuthenticated = computed(() => authStore.isAuthenticated);
const isAuthPage = computed(
  () => route.name === "Login" || route.name === "Register",
);
const isAdmin = computed(() => authStore.isAdmin);
</script>

<template>
  <aside
    v-if="!isAuthPage"
    class="flex min-h-full flex-col border-r border-slate-200 bg-white"
  >
    <div
      class="flex h-20 items-center justify-between border-b border-slate-200 px-5"
    >
      <RouterLink to="/compose" class="flex items-center">
        <img
          src="/notifyLogo.png"
          alt="Notify"
          class="h-20 w-auto object-contain"
        />
      </RouterLink>
      <LayoutDashboard class="h-5 w-5 text-slate-400" />
    </div>

    <div class="p-5">
      <div
        class="flex items-center gap-2 rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm text-slate-400 shadow-sm"
      >
        <Search class="h-4 w-4" />
        <span>Search</span>
        <span
          class="ml-auto rounded-md border border-slate-200 px-1.5 py-0.5 text-[10px]"
          >⌘ F</span
        >
      </div>
    </div>

    <nav class="flex-1 px-5">
      <p
        class="px-3 text-xs font-semibold uppercase tracking-wider text-slate-400"
      >
        Main
      </p>
      <div class="mt-3 space-y-1">
        <RouterLink
          to="/compose"
          class="flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm text-slate-600 transition hover:bg-blue-50 hover:text-blue-600"
          active-class="bg-blue-50 text-blue-600 ring-1 ring-blue-100"
        >
          <PenLine class="h-4 w-4" />
          Compose
        </RouterLink>
        <RouterLink
          to="/history"
          class="flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm text-slate-600 transition hover:bg-blue-50 hover:text-blue-600"
          active-class="bg-blue-50 text-blue-600 ring-1 ring-blue-100"
        >
          <Clock3 class="h-4 w-4" />
          Historique
        </RouterLink>
        <RouterLink
          v-if="isAdmin"
          to="/admin/users"
          class="flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm text-slate-600 transition hover:bg-blue-50 hover:text-blue-600"
          active-class="bg-blue-50 text-blue-600 ring-1 ring-blue-100"
        >
          <ShieldCheck class="h-4 w-4" />
          Administration
        </RouterLink>
        <RouterLink
          to="/admin/templates"
          v-if="isAdmin"
          class="flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm text-slate-600 transition hover:bg-blue-50 hover:text-blue-600"
          active-class="bg-blue-50 text-blue-600 ring-1 ring-blue-100"
        >
          <FileText class="h-4 w-4" />
          Templates
        </RouterLink>
        <RouterLink
          to="/login"
          v-if="!isAuthenticated"
          class="flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm text-slate-600 transition hover:bg-blue-50 hover:text-blue-600"
        >
          <LogIn class="h-4 w-4" />
          Login
        </RouterLink>
        <RouterLink
          to="/register"
          v-if="!isAuthenticated"
          class="flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm text-slate-600 transition hover:bg-blue-50 hover:text-blue-600"
        >
          <UserPlus class="h-4 w-4" />
          Register
        </RouterLink>
      </div>
    </nav>
  </aside>
</template>
