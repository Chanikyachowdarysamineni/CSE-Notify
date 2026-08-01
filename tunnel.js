const localtunnel = require('localtunnel');

(async () => {
  let reconnecting = false;

  async function startTunnel() {
    try {
      const tunnel = await localtunnel({ port: 5000, subdomain: 'lucky-mule-78' });
      console.log('Tunnel URL:', tunnel.url);

      tunnel.on('close', () => {
        console.log('Tunnel closed! Reconnecting...');
        if (!reconnecting) {
          reconnecting = true;
          setTimeout(() => {
            reconnecting = false;
            startTunnel();
          }, 2000);
        }
      });
      
      tunnel.on('error', (err) => {
         console.error('Tunnel error:', err);
      });
    } catch (err) {
      console.error('Failed to start tunnel:', err);
      setTimeout(startTunnel, 5000);
    }
  }

  startTunnel();
  
  // Prevent Node from ever exiting
  setInterval(() => {}, 10000);
})();
