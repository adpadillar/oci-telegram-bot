# Installation and Maintenance Guide

## Overview

This guide provides detailed instructions for installing, deploying, and maintaining the OCI React Samples application, which consists of a React.js frontend and a Spring Boot backend running on Oracle Cloud Infrastructure (OCI).

## Prerequisites

### Required Software

- Java 11 or later
- Node.js 18 or later
- Maven 3.8.5 or later
- Docker (for containerized deployment)
- Oracle Cloud Infrastructure (OCI) account
- OCI CLI
- kubectl (for Kubernetes deployment)
- Terraform (for infrastructure provisioning)

### OCI Requirements

- Access to OCI Autonomous Database
- OCI Container Engine for Kubernetes (OKE)
- OCI Object Storage
- OCI Registry (OCIR)

## OCI Cloud Environment Setup

### 1. Access OCI Cloud Shell

1. Log in to the OCI Console
2. Click the Cloud Shell icon in the top-right corner
3. Wait for the Cloud Shell to initialize

### 2. Clone and Setup

```bash
# Clone the repository
git clone https://github.com/adpadillar/oci-telegram-bot.git
cd oci-telegram-bot

# Run the setup script
chmod +x setup.sh
./setup.sh
```

The setup script will:

- Pull the latest changes
- Verify installed components (Node.js, Java, Maven)
- Guide you through creating the .env file
- Help you extract the wallet
- Start the development environment

## Installation Steps

### 1. Database Setup

1. Create an Autonomous Database in OCI
2. Download the database wallet
3. Extract the wallet to the project root:

```bash
unzip wallet.zip
```

### 2. Environment Configuration

Create `.env` file in `MtdrSpring/backend/`:

```bash
# Database configuration
SPRING_DATASOURCE_URL=jdbc:oracle:thin:@your_db_name_high?TNS_ADMIN=/path/to/wallet
SPRING_DATASOURCE_USERNAME=your_db_username
SPRING_DATASOURCE_PASSWORD=your_db_password

# Telegram bot configuration (optional)
TELEGRAM_BOT_NAME=your_bot_name
TELEGRAM_BOT_TOKEN=your_bot_token

# Server configuration
SERVER_PORT=8081
```

### 3. Development Setup

```bash
cd MtdrSpring/backend
chmod +x dev.sh
./dev.sh
```

### 4. Production Deployment

#### Using Docker

```bash
cd MtdrSpring/backend
docker build -t your-registry/oci-react-app:latest .
docker push your-registry/oci-react-app:latest
```

#### Using Kubernetes

```bash
kubectl apply -f todolistapp-springboot.yaml
```

## Maintenance

### Database Maintenance

1. Regular Backups

   - Enable automated backups in OCI Autonomous Database
   - Monitor backup status through OCI Console

2. Performance Monitoring
   - Monitor database metrics through OCI Console
   - Review and optimize slow queries
   - Monitor connection pool usage

### Application Maintenance

1. Logging

   - Application logs are stored in `logs.log`
   - Monitor logs for errors and performance issues
   - Configure log rotation if needed

2. Security Updates

   - Regularly update dependencies:

     ```bash
     # Backend
     cd MtdrSpring/backend
     mvn versions:display-dependency-updates

     # Frontend
     cd src/main/frontend
     npm audit
     npm update
     ```

3. Scaling
   - Monitor application performance
   - Adjust Kubernetes resources as needed
   - Scale database resources through OCI Console

### Monitoring and Alerts

1. Set up monitoring for:

   - Application health
   - Database performance
   - Kubernetes cluster metrics
   - API response times

2. Configure alerts for:
   - High CPU/Memory usage
   - Database connection issues
   - Application errors
   - API latency spikes

## Troubleshooting

### Common Issues

1. Database Connection Issues

   - Verify wallet configuration
     ```bash
     # Check if wallet files exist
     ls -la wallet/
     # Should show: cwallet.sso, ewallet.p12, keystore.jks, ojdbc.properties, sqlnet.ora, tnsnames.ora, truststore.jks
     ```
   - Check database credentials in .env file
   - Ensure network connectivity
   - Verify TNS_ADMIN path in SPRING_DATASOURCE_URL
   - Check if database is running:
     ```bash
     oci db autonomous-database get --autonomous-database-id <db-ocid>
     ```

2. Application Startup Failures

   - Check environment variables
     ```bash
     # Verify all required variables are set
     cat MtdrSpring/backend/.env
     ```
   - Verify database connectivity
     ```bash
     # Test database connection
     sqlplus admin/<password>@<db_name>_high
     ```
   - Review application logs
     ```bash
     # Check application logs
     tail -f logs.log
     ```
   - Check port availability
     ```bash
     # Verify port 8081 is not in use
     netstat -tulpn | grep 8081
     ```

3. Frontend Issues

   - Clear browser cache
   - Check API connectivity
     ```bash
     # Test API endpoint
     curl http://localhost:8081/api/health
     ```
   - Verify environment configuration
   - Check Vite development server
     ```bash
     # Restart Vite server
     cd MtdrSpring/backend/src/main/frontend
     npm run dev
     ```

4. Kubernetes Deployment Issues

   - Check pod status
     ```bash
     kubectl get pods -n mtdrworkshop
     kubectl describe pod <pod-name> -n mtdrworkshop
     ```
   - Verify secrets
     ```bash
     kubectl get secrets -n mtdrworkshop
     kubectl describe secret db-wallet-secret -n mtdrworkshop
     ```
   - Check service configuration
     ```bash
     kubectl get services -n mtdrworkshop
     kubectl describe service todolistapp-springboot -n mtdrworkshop
     ```

5. Build and Dependency Issues

   - Maven build failures
     ```bash
     # Clean and rebuild
     cd MtdrSpring/backend
     mvn clean install -DskipTests
     ```
   - Node.js dependency issues
     ```bash
     # Clear npm cache and reinstall
     cd MtdrSpring/backend/src/main/frontend
     npm cache clean --force
     rm -rf node_modules
     npm install
     ```

6. Telegram Bot Issues

   - Verify bot token
     ```bash
     # Check if bot token is set
     echo $TELEGRAM_BOT_TOKEN
     ```
   - Test bot connection
     ```bash
     # Send test message to bot
     curl -X POST https://api.telegram.org/bot<YOUR_BOT_TOKEN>/sendMessage \
       -d "chat_id=<YOUR_CHAT_ID>&text=Test message"
     ```

7. OCI Resource Issues
   - Check OCI CLI configuration
     ```bash
     oci setup verify
     ```
   - Verify compartment access
     ```bash
     oci iam compartment list
     ```
   - Check resource quotas
     ```bash
     oci limits quota list --compartment-id <compartment-ocid>
     ```

### Debugging Tools

1. Application Logs

   ```bash
   # View real-time logs
   tail -f logs.log

   # Search for specific errors
   grep -i "error" logs.log
   ```

2. Database Debugging

   ```bash
   # Check database sessions
   sqlplus admin/<password>@<db_name>_high
   SELECT username, status, count(*) FROM v$session GROUP BY username, status;

   # Check database locks
   SELECT * FROM v$locked_object;
   ```

3. Network Debugging

   ```bash
   # Check network connectivity
   telnet localhost 8081

   # Test API endpoints
   curl -v http://localhost:8081/api/health
   ```

4. Kubernetes Debugging

   ```bash
   # Get pod logs
   kubectl logs <pod-name> -n mtdrworkshop

   # Check events
   kubectl get events -n mtdrworkshop

   # Debug pod
   kubectl exec -it <pod-name> -n mtdrworkshop -- /bin/bash
   ```

### Performance Issues

1. Database Performance

   - Check slow queries
     ```sql
     SELECT sql_id, sql_text, elapsed_time/1000000 as elapsed_secs
     FROM v$sql
     WHERE elapsed_time > 1000000
     ORDER BY elapsed_time DESC;
     ```
   - Monitor connection pool
     ```bash
     # Check UCP pool status
     curl http://localhost:8081/actuator/health
     ```

2. Application Performance

   - Monitor JVM metrics
     ```bash
     # Check JVM memory usage
     jps -l
     jmap -heap <pid>
     ```
   - Check thread dumps
     ```bash
     jstack <pid> > thread_dump.txt
     ```

3. Frontend Performance
   - Check browser console for errors
   - Monitor network requests
   - Verify bundle size
     ```bash
     cd MtdrSpring/backend/src/main/frontend
     npm run build -- --report
     ```

## Security Considerations

1. Secrets Management

   - Use OCI Vault for sensitive data
   - Rotate database credentials regularly
   - Secure API keys and tokens

2. Network Security

   - Configure proper network policies
   - Use SSL/TLS for all connections
   - Implement proper firewall rules

3. Access Control
   - Implement proper authentication
   - Use role-based access control
   - Regular security audits

## Backup and Recovery

1. Database Backups

   - Regular automated backups
   - Point-in-time recovery capability
   - Test recovery procedures

2. Application Data
   - Regular backups of configuration
   - Document recovery procedures
   - Test backup restoration

## Support and Resources

- OCI Documentation: https://docs.oracle.com/en-us/iaas/Content/home.htm
- Spring Boot Documentation: https://spring.io/projects/spring-boot
- React Documentation: https://reactjs.org/docs/getting-started.html
- Kubernetes Documentation: https://kubernetes.io/docs/home/
