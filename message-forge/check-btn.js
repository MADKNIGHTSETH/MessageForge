import puppeteer from 'puppeteer';

(async () => {
  const browser = await puppeteer.launch({ args: ['--no-sandbox'] });
  const page = await browser.newPage();
  
  page.on('console', msg => console.log('PAGE LOG:', msg.text()));
  page.on('pageerror', err => console.log('PAGE ERROR:', err.message));

  try {
    // 1. Load App and Login
    await page.goto('http://localhost:5173/login');
    await page.type('input[type="email"]', 'admin@messageforge.local');
    await page.type('input[type="password"]', 'Password123!');
    await page.click('button.bg-sky-600');
    await page.waitForNavigation({ waitUntil: 'networkidle0', timeout: 5000 }).catch(() => {});
    
    // 2. Open Profile Menu
    console.log("Current URL after login:", page.url());
    await page.click('header .flex.cursor-pointer');
    await new Promise(r => setTimeout(r, 500));
    
    console.log("=== Profile menu opened ===");
    const textBefore = await page.$eval('header', el => el.innerText);
    console.log("Header text before click:\n", textBefore.replace(/\n/g, ' '));
    
    // 3. Click 'Connecter un nouveau compte'
    const btn = await page.$x("//button[contains(., 'Connecter un nouveau compte')]");
    if (btn.length > 0) {
      console.log("Button found, clicking...");
      await btn[0].click();
      await new Promise(r => setTimeout(r, 500));
      
      const textAfter = await page.$eval('header', el => el.innerText);
      console.log("Header text after click:\n", textAfter.replace(/\n/g, ' '));
    } else {
      console.log("Button not found!");
    }
    
  } catch (e) {
    console.log('SCRIPT ERROR:', e.message);
  }
  
  await browser.close();
})();