const BASE_URL = "http://localhost:8085/api";

async function runTests() {
  let adminToken = "";
  let userToken = "";

  const req = async (name, method, path, body, token) => {
    const headers = {};
    if (body) headers["Content-Type"] = "application/json";
    if (token) headers["Authorization"] = `Bearer ${token}`;

    try {
      const res = await fetch(`${BASE_URL}${path}`, {
        method,
        headers,
        body: body ? JSON.stringify(body) : undefined
      });
      
      const text = await res.text();
      let resBody = null;
      try { resBody = JSON.parse(text); } catch(e) {}
      
      console.log(`[${res.status}] ${method.padEnd(6)} ${path.padEnd(35)} - ${name}`);
      return { status: res.status, body: resBody };
    } catch (e) {
      console.log(`[ERROR] ${method} ${path} - ${name}: ${e.message}`);
      return { status: 0, body: null };
    }
  };

  console.log("=== Authentication Setup ===");
  // Login as Admin
  const adminLogin = await req("Admin Login", "POST", "/auth/login", { 
    email: "admin@messageforge.local", 
    password: "Password123!" 
  });
  if (adminLogin.status === 200) adminToken = adminLogin.body.accessToken;

  // Login as User
  const userLogin = await req("User Login", "POST", "/auth/login", { 
    email: "test@example.com", 
    password: "Password123!" 
  });
  if (userLogin.status === 200) userToken = userLogin.body.accessToken;

  if (!adminToken || !userToken) {
    console.log("Failed to acquire tokens. Aborting tests.");
    return;
  }

  console.log("\n=== Testing Profile Updates (User) ===");
  await req("Update Profile", "PUT", "/auth/me", { 
    displayName: "Updated Test User",
    avatarUrl: "https://example.com/avatar.png"
  }, userToken);
  
  await req("Verify Profile Update", "GET", "/auth/me", null, userToken);

  console.log("\n=== Testing Admin Stats (Admin) ===");
  await req("Get Global Stats", "GET", "/admin/stats", null, adminToken);

  console.log("\n=== Testing Admin Templates (Admin) ===");
  let templateId = null;
  
  const createTpl = await req("Create Template", "POST", "/admin/templates", {
    channelType: "EMAIL",
    name: "Welcome Email",
    templateBody: "<h1>Welcome</h1><p>{content}</p>",
    isDefault: true
  }, adminToken);
  
  if (createTpl.status === 200 && createTpl.body?.id) {
    templateId = createTpl.body.id;
  }

  await req("List Templates", "GET", "/admin/templates", null, adminToken);

  if (templateId) {
    await req("Delete Template", "DELETE", `/admin/templates/${templateId}`, null, adminToken);
  }

  console.log("\nTests completed.");
}

runTests();