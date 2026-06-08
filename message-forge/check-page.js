import puppeteer from 'puppeteer';

(async () => {
  const browser = await puppeteer.launch({ args: ['--no-sandbox'] });
  const page = await browser.newPage();
  
  page.on('console', msg => {
    if (msg.type() === 'error') console.log('PAGE ERROR LOG:', msg.text());
  });
  page.on('pageerror', err => console.log('UNCAUGHT EXCEPTION:', err.message));

  try {
    // Navigate to a blank page on the same origin first to set localStorage
    await page.goto('http://localhost:5173/favicon.ico', { waitUntil: 'networkidle0', timeout: 5000 });
    
    // Inject a mock JWT
    await page.evaluate(() => {
      localStorage.setItem('messageforge_auth', JSON.stringify({
        token: "mock.token.123",
        user: { id: "1", email: "admin@messageforge.local", role: "ADMIN" }
      }));
    });

    // Now visit the app
    await page.goto('http://localhost:5173', { waitUntil: 'networkidle0', timeout: 5000 });
    await new Promise(r => setTimeout(r, 1000));
    const content = await page.$eval('body', el => el.innerText);
    console.log("BODY TEXT:\n", content.substring(0, 500));
  } catch (e) {
    console.log('SCRIPT ERROR:', e.message);
  }
  
  await browser.close();
})();