<script setup>
import { ref, computed } from "vue";
import { useRoute } from "vue-router";
import {
  ChevronDown,
  UserCircle,
  Plus,
  Mail,
  Link as LinkIcon,
  Phone,
  LogOut,
  MessageSquare,
  Globe,
  X as XIcon,
  MessageCircle,
} from "@lucide/vue";
import NavBar from "./components/NavBar.vue";
import { useAuthStore } from "./stores/auth";
import { useRouter } from "vue-router";

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const isAuthPage = computed(
  () => route.name === "Login" || route.name === "Register",
);
const displayName = computed(
  () => authStore.user?.displayName || "Ralph Edwards",
);
const email = computed(() => authStore.user?.email || "edwards@gmail.com");

const isProfileOpen = ref(false);
const addAccountStep = ref("list"); // 'list', 'selectType', 'login'
const selectedNewAccountType = ref("");
const accountError = ref("");
const newAccountForm = ref({
  value: "",
  label: "",
  password: "",
});

const connectedAccounts = computed(() => authStore.connectedAccounts);

const typeIcons = {
  Email: Mail,
  LinkedIn: LinkIcon,
  SMS: Phone,
  Slack: MessageSquare,
  X: XIcon,
  Messenger: MessageCircle,
  WhatsApp: Globe,
};

const toggleProfile = () => {
  isProfileOpen.value = !isProfileOpen.value;
  if (!isProfileOpen.value) {
    resetAddAccount();
  }
};

const resetAddAccount = () => {
  addAccountStep.value = "list";
  selectedNewAccountType.value = "";
  accountError.value = "";
  newAccountForm.value = { value: "", label: "", password: "" };
};

const startAddAccount = () => {
  addAccountStep.value = "selectType";
};

const selectAccountType = (type) => {
  selectedNewAccountType.value = type;
  addAccountStep.value = "login";
};

const submitNewAccount = async () => {
  accountError.value = "";
  if (
    !newAccountForm.value.value ||
    !newAccountForm.value.label ||
    !newAccountForm.value.password
  )
    return;

  try {
    await authStore.addAccount({
      type: selectedNewAccountType.value,
      value: newAccountForm.value.value,
      label: newAccountForm.value.label,
      password: newAccountForm.value.password,
    });
    resetAddAccount();
  } catch (error) {
    accountError.value = error.message || "Connexion du compte impossible.";
  }
};

const logout = async () => {
  await authStore.logout();
  router.push("/login");
  isProfileOpen.value = false;
};
</script>

<template>
  <div
    class="min-h-screen bg-gradient-to-br from-sky-200 via-sky-100 to-blue-300 p-4 text-slate-900 sm:p-6"
  >
    <div
      :class="[
        'mx-auto min-h-[calc(100vh-2rem)] overflow-hidden rounded-[28px] border border-white/80 bg-white shadow-2xl shadow-blue-300/30 sm:min-h-[calc(100vh-3rem)]',
        isAuthPage ? 'max-w-5xl' : 'max-w-7xl lg:grid lg:grid-cols-[236px_1fr]',
      ]"
    >
      <NavBar />

      <section class="min-w-0 bg-[#eef7ff]">
        <header
          v-if="!isAuthPage"
          class="flex min-h-20 items-center justify-end border-b border-slate-200/80 bg-white px-5"
        >
          <div class="flex items-center gap-5">
            <div class="relative">
              <div
                @click="toggleProfile"
                class="flex cursor-pointer items-center gap-3 rounded-2xl p-1 transition hover:bg-slate-50"
              >
                <div
                  class="grid h-11 w-11 place-items-center overflow-hidden rounded-full bg-blue-100 text-blue-600"
                >
                  <UserCircle class="h-8 w-8" />
                </div>
                <div class="hidden leading-tight sm:block">
                  <p class="text-sm font-semibold text-slate-900">
                    {{ displayName }}
                  </p>
                  <p class="text-xs text-slate-500">{{ email }}</p>
                </div>
                <ChevronDown
                  class="h-4 w-4 text-slate-500 transition-transform"
                  :class="{ 'rotate-180': isProfileOpen }"
                />
              </div>

              <!-- Profile Dropdown with Multi-step Accounts -->
              <div
                v-if="isProfileOpen"
                class="absolute right-0 top-full z-50 mt-2 w-80 rounded-2xl border border-sky-100 bg-white p-5 shadow-xl shadow-blue-900/10"
              >
                <!-- Step 1: Account List -->
                <div v-if="addAccountStep === 'list'">
                  <p
                    class="mb-3 text-[11px] font-bold uppercase tracking-widest text-slate-400"
                  >
                    Comptes Connectés
                  </p>
                  <div class="space-y-2">
                    <div
                      v-for="acc in connectedAccounts"
                      :key="acc.id"
                      class="flex items-center gap-3 rounded-xl border border-sky-50 bg-sky-50/50 p-2 text-sm"
                    >
                      <div
                        class="grid h-8 w-8 place-items-center rounded-lg bg-white text-sky-600 shadow-sm"
                      >
                        <component :is="typeIcons[acc.type]" class="h-4 w-4" />
                      </div>
                      <div class="min-w-0 flex-1">
                        <p class="truncate font-medium text-slate-900">
                          {{ acc.label }}
                        </p>
                        <p class="truncate text-[10px] text-slate-500">
                          {{ acc.value }}
                        </p>
                      </div>
                    </div>
                    <button
                      @click="startAddAccount"
                      class="mt-2 flex w-full items-center justify-center gap-2 rounded-xl border border-dashed border-sky-300 py-2.5 text-xs font-medium text-sky-600 transition hover:bg-sky-50"
                    >
                      <Plus class="h-3 w-3" />
                      Connecter un nouveau compte
                    </button>
                  </div>
                </div>

                <!-- Step 2: Select Type -->
                <div
                  v-else-if="addAccountStep === 'selectType'"
                  class="space-y-4"
                >
                  <div class="flex items-center gap-2">
                    <button
                      @click="addAccountStep = 'list'"
                      class="text-slate-400 hover:text-slate-600"
                    >
                      <Plus class="h-4 w-4 rotate-45" />
                    </button>
                    <p class="text-sm font-bold text-slate-900">
                      Choisir le type
                    </p>
                  </div>
                  <div class="grid grid-cols-2 gap-2">
                    <button
                      v-for="type in [
                        'Email',
                        'SMS',
                        'LinkedIn',
                        'Slack',
                        'X',
                        'Messenger',
                        'WhatsApp',
                      ]"
                      :key="type"
                      @click="selectAccountType(type)"
                      class="flex items-center gap-3 rounded-xl border border-slate-100 bg-slate-50 p-2.5 text-xs transition hover:bg-sky-50 hover:text-sky-700 hover:border-sky-100"
                    >
                      <component :is="typeIcons[type]" class="h-3.5 w-3.5" />
                      {{ type }}
                    </button>
                  </div>
                </div>

                <!-- Step 3: Login Form -->
                <div v-else-if="addAccountStep === 'login'" class="space-y-4">
                  <div class="flex items-center gap-2">
                    <button
                      @click="addAccountStep = 'selectType'"
                      class="text-slate-400 hover:text-slate-600"
                    >
                      <Plus class="h-4 w-4 rotate-45" />
                    </button>
                    <p class="text-sm font-bold text-slate-900">
                      Connexion {{ selectedNewAccountType }}
                    </p>
                  </div>
                  <div class="space-y-3">
                    <div class="space-y-1">
                      <label
                        class="text-[10px] font-bold uppercase text-slate-400"
                        >Nom du compte</label
                      >
                      <input
                        v-model="newAccountForm.label"
                        type="text"
                        placeholder="ex: Perso, Pro..."
                        class="w-full rounded-xl border border-slate-100 bg-slate-50 px-3 py-2 text-sm outline-none focus:border-sky-500 transition shadow-sm"
                      />
                    </div>
                    <div class="space-y-1">
                      <label
                        class="text-[10px] font-bold uppercase text-slate-400"
                      >
                        {{
                          selectedNewAccountType === "SMS" ||
                          selectedNewAccountType === "WhatsApp"
                            ? "Numéro de téléphone"
                            : selectedNewAccountType === "Email"
                              ? "Adresse Email"
                              : "Identifiant / Username"
                        }}
                      </label>
                      <input
                        v-model="newAccountForm.value"
                        :type="
                          selectedNewAccountType === 'Email' ? 'email' : 'text'
                        "
                        :placeholder="
                          selectedNewAccountType === 'SMS' ||
                          selectedNewAccountType === 'WhatsApp'
                            ? '+33 6...'
                            : '...'
                        "
                        class="w-full rounded-xl border border-slate-100 bg-slate-50 px-3 py-2 text-sm outline-none focus:border-sky-500 transition shadow-sm"
                      />
                    </div>
                    <div class="space-y-1">
                      <label
                        class="text-[10px] font-bold uppercase text-slate-400"
                      >
                        {{
                          selectedNewAccountType === "SMS" ||
                          selectedNewAccountType === "WhatsApp"
                            ? "Code de validation"
                            : "Mot de passe"
                        }}
                      </label>
                      <input
                        v-model="newAccountForm.password"
                        :type="
                          selectedNewAccountType === 'SMS' ||
                          selectedNewAccountType === 'WhatsApp'
                            ? 'text'
                            : 'password'
                        "
                        :placeholder="
                          selectedNewAccountType === 'SMS' ||
                          selectedNewAccountType === 'WhatsApp'
                            ? '000000'
                            : '••••••••'
                        "
                        class="w-full rounded-xl border border-slate-100 bg-slate-50 px-3 py-2 text-sm outline-none focus:border-sky-500 transition shadow-sm"
                      />
                    </div>
                    <button
                      @click="submitNewAccount"
                      class="w-full rounded-xl bg-sky-600 py-2.5 text-sm font-semibold text-white transition hover:bg-sky-700 shadow-md active:scale-[0.98]"
                    >
                      Se connecter
                    </button>
                    <div
                      v-if="accountError"
                      class="rounded-xl bg-rose-50 px-3 py-2 text-xs text-rose-700"
                    >
                      {{ accountError }}
                    </div>
                  </div>
                </div>

                <div class="mt-4 border-t border-slate-100 pt-3">
                  <button
                    @click="logout"
                    class="flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium text-rose-600 transition hover:bg-rose-50"
                  >
                    <LogOut class="h-4 w-4" />
                    Se déconnecter
                  </button>
                </div>
              </div>
            </div>
          </div>
        </header>

        <main
          :class="
            isAuthPage
              ? 'grid min-h-[calc(100vh-2rem)] place-items-center p-6 sm:min-h-[calc(100vh-3rem)]'
              : 'p-5 lg:p-6'
          "
        >
          <router-view />
        </main>
      </section>
    </div>
  </div>
</template>

<style>
body {
  margin: 0;
  font-family:
    Inter,
    ui-sans-serif,
    system-ui,
    -apple-system,
    BlinkMacSystemFont,
    "Segoe UI",
    sans-serif;
  background: #bfdbfe;
}
</style>
