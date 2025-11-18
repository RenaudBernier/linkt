# Linkt - Campus Events & Ticketing Web Application

A comprehensive web application designed to help students discover, organize, and attend events on campus. The system streamlines event management, improves student engagement, and provides valuable insights for both organizers and campus administration.

## Overview

This platform enables students to browse events, save them to their personal calendar, claim free or paid tickets, and check in using QR codes. Organizers can create and manage events, track attendance, and access analytics through dashboards, while administrators moderate content and oversee organizations.

## Core Features

### 1. Student Event Experience

#### Event Discovery
- Browse and search events with comprehensive filters
  - Date range filtering
  - Event category selection
  - Organization-based filtering
- Interactive event listings with detailed information

#### Event Management
- Save events to personal calendar
- Claim tickets (free or mock paid)
- Receive digital tickets with unique QR codes
- View ticket history and upcoming events

### 2. Organizer Event Management

#### Event Creation
- Comprehensive event setup with:
  - Event title and description
  - Date and time scheduling
  - Location specification
  - Ticket capacity management
  - Ticket type configuration (free or paid)

#### Event Analytics
- Dedicated dashboard per event featuring:
  - Tickets issued statistics
  - Attendance rates tracking
  - Remaining capacity monitoring

#### Management Tools
- Export attendee lists in CSV format
- Integrated QR scanner for ticket validation
- Event modification and cancellation capabilities

### 3. Administrator Dashboard & Moderation

#### Platform Oversight
- Organizer account approval system
- Event listing moderation for policy compliance
- Content management and quality control

#### Analytics & Reporting
- Global platform statistics:
  - Total number of events
  - Tickets issued across all events
  - Participation trends and insights

#### Management Features
- Organization management and oversight
- Role assignment and permissions
- User account administration

### 4. Location-Based Features

- Interactive map interface showing nearby events
- Real estate website-style map visualization
- Location-based event recommendations
- Proximity-based event filtering

## Technical Stack

### Frontend
- **Framework**: React 19.1.1 with TypeScript
- **Build Tool**: Vite 7.1.2
- **UI Library**: Material-UI (MUI) 7.3.2
- **Styling**: Emotion (CSS-in-JS)
- **Routing**: React Router DOM 7.9.3
- **HTTP Client**: Axios 1.12.2
- **Notifications**: Notistack 3.0.2
- **QR Code Generation**: qrcode.react 4.2.0

### Backend
- **Framework**: Spring Boot 3.5.5
- **Language**: Java 25
- **Build Tool**: Maven
- **Database**: SQLite 3.47.1.0 with Hibernate ORM
- **Authentication**: JWT (JSON Web Tokens) with Spring Security
- **API**: RESTful API with Spring Web
- **Validation**: Spring Boot Starter Validation

### Database
- **SQLite**: Lightweight, file-based relational database
- **Schema Management**: Python script for database initialization and seeding

## Getting Started

### Prerequisites
- Java 25 or higher
- Python 3.x
- Node.js and npm
- Maven

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd linkt
   ```

2. **Install frontend dependencies**
   ```bash
   cd frontend/my-react-app
   npm install
   cd ../..
   ```

3. **Backend dependencies** (Maven will handle this automatically)

## Usage

### 1. Create and Initialize Database

Navigate to the database directory and run the Python script to create the SQLite database with seed data:

```bash
python3 backend/database/create_database.py
```

This will:
- Create a new `linkt.db` file in the database directory
- Set up all required tables (user, event, ticket, saved_event)
- Populate the database with sample data including:
  - 3 organizers (2 approved, 1 pending)
  - 3 students
  - 1 administrator
  - 3 sample events
  - Sample tickets and saved events

### 2. Configure Environment Variables

Create a `.env` file in the `backend/springboot-app` directory with your SendGrid API key for email functionality:

```bash
cd backend/springboot-app
cat > .env << EOF
# SendGrid Configuration
SENDGRID_API_KEY=your_sendgrid_api_key_here
EOF
``` 

**Note:** The `.env` file is already included in `.gitignore` to keep your API key secure. Never commit this file to version control.

### 3. Start the Backend Server

From the root directory:

```bash
cd backend/springboot-app
mvn spring-boot:run
```

The backend server will start on `http://localhost:8080`

### 4. Start the Frontend Development Server

In a new terminal, from the root directory:

```bash
cd frontend/my-react-app
npm run dev
```

The frontend application will be available at `http://localhost:5173` (or the next available port)

### 5. Access the Application

Open your browser and navigate to the frontend URL.

## Contributing

*[Contribution guidelines to be added]*

## License

*[License information to be added]*

---

## Team Members

<table>
  <thead>
    <tr>
      <th align="left">Name</th>
      <th align="center">Student ID</th>
      <th align="left">Role</th>
      <th align="left">GitHub User Name</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>Thomas Ballard</td>
      <td>40263348</td>
      <td> Full-Stack </td>
      <td>thomasballarddev</td>
    </tr>
    <tr>
      <td>Levon Kadehjian</td>
      <td>40268535</td>
      <td> Full-Stack </td>
      <td>Levon-Kadehjian</td>
    </tr>
    <tr>
      <td> Darcy Loane-Billings </td>
      <td> 40310186 </td>
      <td> Full-Stack </td>
      <td>DudeNamedDarcy</td>
    </tr>
    <tr>
      <td>Peter Fitopoulos</td>
      <td>40316056</td>
      <td> Full-Stack </td>
      <td>mvpete1</td>
    </tr>
    <tr>
      <td>Daniel Buta</td>
      <td>40300680</td>
      <td> Full-Stack </td>
     <td>daniel-buta</td>
    </tr>
    <tr>
      <td>Maximilian Ingram</td>
      <td>40329376</td>
      <td> Full-Stack </td>
      <td>MaxIngram05</td>
    </tr>
    <tr>
      <td>Renaud Bernier</td>
      <td>40212192</td>
      <td> Scrum Master and Full-Stack </td>
      <td>RenaudBernier</td>
    </tr>
  </tbody>
</table>
