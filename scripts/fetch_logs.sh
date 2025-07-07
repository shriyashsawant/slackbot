#!/bin/bash
echo "Recent Logs:"
tail -n 10 /var/log/syslog 2>/dev/null || echo "No logs available"
