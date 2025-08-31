# API Endpoints Documentation (Detailed)

## 1. Ping

**Endpoint:** `/ping`
**Method:** `GET`

**Description:** Returns a ping.

---

## 2. Agent Discovery

**Endpoint:** `/agents`
**Method:** `GET`

**Description:** Returns a list of agents.

**Parameters:**

- **limit** (query): Maximum number of agents to return.

    - Type: `integer`
    - Default: `10`
    - Minimum: `1`
    - Maximum: `1000`

- **offset** (query): Number of agents to skip.

    - Type: `integer`
    - Default: `0`
    - Minimum: `0`

---

## 3. Agent Manifest

**Endpoint:** `/agents/{name}`
**Method:** `GET`

**Description:** Returns a manifest of the specified agent.

**Parameters:**

- **name** (path): The name of the agent to retrieve.

    - Type: `object`

---

## 4. Create a new run

**Endpoint:** `/runs`
**Method:** `POST`

**Description:** Create and start a new run for the specified agent.

**Request Body:**

- Content-Type: `application/json`
    - Schema: `RunCreateRequest`

    - **Fields:**

        * **agent_name**: Nested object
        * **session_id**: Nested object
        * **session**: Nested object
        * **input** (array): No description provided
        * **mode**: Nested object

---

## 5. Get run status

**Endpoint:** `/runs/{run_id}`
**Method:** `GET`

**Description:** Returns the current status and details of a run.

**Parameters:**

- **run_id** (path): UUID of the run.

    - Type: `object`

---

## 6. Resume a run

**Endpoint:** `/runs/{run_id}`
**Method:** `POST`

**Description:** Resume a paused or awaiting run.

**Request Body:**

- Content-Type: `application/json`
    - Schema: `RunResumeRequest`

    - **Fields:**

        * **run_id**: Nested object
        * **await_resume**: Nested object
        * **mode**: Nested object

---

## 7. Cancel a run

**Endpoint:** `/runs/{run_id}/cancel`
**Method:** `POST`

**Description:** Cancel the specified run.

**Parameters:**

- **run_id** (path): UUID of the run to cancel.

    - Type: `object`

---

## 8. List run events

**Endpoint:** `/runs/{run_id}/events`
**Method:** `GET`

**Description:** Returns a list of events emitted by the run.

**Parameters:**

- **run_id** (path): UUID of the run.

    - Type: `object`

---

## 9. Session

**Endpoint:** `/session/{session_id}`
**Method:** `GET`

**Description:** Returns details of the specified session.

**Parameters:**

- **name** (path): The id of the session to retrieve.

    - Type: `object`

---
