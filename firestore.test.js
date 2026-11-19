const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_pixie_123";
const BOB_UID = "bob_pixie_456";
const CHARLIE_UID = "charlie_pixie_789";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validGamePayload(uid, overrides = {}) {
  const now = new Date(Date.now() - 1000);
  return {
    userId: uid,
    diem: 0,
    cashDo: 0,
    clickCount: 0,
    muti: 1.0,
    muti2: 1.0,
    x: 1.0,
    cd: 0.25,
    thay: 0.125,
    giamgia: 1.0,
    thay2: 0.1,
    cd3: 1.0,
    cd4: 1.0,
    lv: 0,
    gia: 25.0,
    lv2: 0,
    gia2: 50.0,
    lv3: 0,
    gia3: 150.0,
    lv4: 0,
    gia4: 75.0,
    lv5: 0,
    gia5: 5.0,
    lv6: 0,
    gia6: 3.0,
    lv7: 0,
    gia7: 3.0,
    gioihan: true,
    autoEnabled: false,
    x2: 1.0,
    x2fake: 1.0,
    cd2: 1.0,
    lv8: 0,
    gia8: 3.0,
    lv9: 0,
    gia9: 2.0,
    createdAt: now,
    updatedAt: now,
    ...overrides,
  };
}

function validServerPayload(overrides = {}) {
  const now = new Date(Date.now() - 1000);
  return {
    name: "Pixie Forest #1",
    region: "SEA",
    maxPlayers: 20,
    playerCount: 0,
    activePlayerIds: [],
    createdAt: now,
    updatedAt: now,
    ...overrides,
  };
}

function validServerPlayerPayload(serverId, uid, overrides = {}) {
  return {
    userId: uid,
    serverId,
    displayName: "PixieMage",
    diem: 500,
    cashDo: 12.5,
    restartsLv: 1,
    online: true,
    updatedAt: new Date(Date.now() - 1000),
    ...overrides,
  };
}

function validTradePayload(serverId, senderId, receiverId, overrides = {}) {
  const now = new Date(Date.now() - 1000);
  return {
    serverId,
    senderId,
    senderName: "Alice",
    receiverId,
    receiverName: "Bob",
    participantIds: [senderId, receiverId],
    status: "pending",
    cancelledBy: "",
    senderPoints: 0,
    senderCash: 0,
    receiverPoints: 0,
    receiverCash: 0,
    senderEscrowPoints: 0,
    senderEscrowCash: 0,
    receiverEscrowPoints: 0,
    receiverEscrowCash: 0,
    senderAccepted: false,
    receiverAccepted: false,
    senderClaimed: false,
    receiverClaimed: false,
    senderRefunded: false,
    receiverRefunded: false,
    createdAt: now,
    updatedAt: now,
    ...overrides,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write game progress, servers, or trades", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set(validGamePayload(ALICE_UID))
  );
  await assertFails(unauthDb.collection("servers").where("maxPlayers", "==", 20).get());
  await assertFails(unauthDb.collection("trades").where("participantIds", "array-contains", ALICE_UID).get());
});

test("Owner can create, read, and partially update their own game progress", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 5000);
  const initialPayload = validGamePayload(ALICE_UID, {
    createdAt: created,
    updatedAt: created,
  });

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set(initialPayload));
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      diem: 125.5,
      clickCount: 42,
      lv: 1,
      x: 2.0,
      gia: 75.0,
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Rejects @DocumentId id field, ghost fields, immutable createdAt mutation, and future timestamps", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 5000);

  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set(
      validGamePayload(ALICE_UID, { id: ALICE_UID })
    )
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set(
      validGamePayload(ALICE_UID, { isAdmin: true })
    )
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set(
      validGamePayload(ALICE_UID, { createdAt: created, updatedAt: created })
    )
  );

  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      diem: 999,
      updatedAt: new Date(Date.now() + 60000),
    })
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      diem: -10,
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Cross-user isolation: Alice cannot read, list, or write Bob's progress", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).set(validGamePayload(BOB_UID));
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
  await assertFails(aliceDb.collection("users").get());
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).update({
      diem: 999999,
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Servers enforce 20-player cap, self-only join/leave, and scoped player lists", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  // Seed empty server
  await assertSucceeds(
    aliceDb.collection("servers").doc("server_1").set(validServerPayload())
  );

  // List servers query aligned with rules
  await assertSucceeds(
    aliceDb.collection("servers").where("maxPlayers", "==", 20).get()
  );

  // Alice joins server_1 & creates own player card
  await assertSucceeds(
    aliceDb.collection("servers").doc("server_1").update({
      activePlayerIds: [ALICE_UID],
      playerCount: 1,
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertSucceeds(
    aliceDb.collection("servers").doc("server_1").collection("players").doc(ALICE_UID).set(
      validServerPlayerPayload("server_1", ALICE_UID, { displayName: "Alice" })
    )
  );

  // Alice cannot add Bob into server_1 or kick someone else
  await assertFails(
    aliceDb.collection("servers").doc("server_1").update({
      activePlayerIds: [ALICE_UID, BOB_UID],
      playerCount: 2,
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertFails(
    bobDb.collection("servers").doc("server_1").collection("players").doc(ALICE_UID).update({
      diem: 999999,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Bob lists players inside server_1 using scoped query
  await assertSucceeds(
    bobDb.collection("servers").doc("server_1").collection("players").where("serverId", "==", "server_1").get()
  );

  // Test strict 20-player maximum: 20 players already inside -> 21st rejected
  const twentyUids = Array.from({ length: 20 }, (_, i) => `player_${i + 1}`);
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const created = new Date(Date.now() - 5000);
    await context.firestore().collection("servers").doc("server_full").set({
      name: "Full Realm #9",
      region: "Global",
      maxPlayers: 20,
      playerCount: 20,
      activePlayerIds: twentyUids,
      createdAt: created,
      updatedAt: created,
    });
  });

  await assertFails(
    aliceDb.collection("servers").doc("server_full").update({
      activePlayerIds: [...twentyUids, ALICE_UID],
      playerCount: 21,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Alice leaves server_1 cleanly
  await assertSucceeds(
    aliceDb.collection("servers").doc("server_1").update({
      activePlayerIds: [],
      playerCount: 0,
      updatedAt: new Date(Date.now() - 400),
    })
  );
});

test("Trades enforce participant scoping, non-empty dual agreement, offer edit resets, and claim/cancel", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const charlieDb = testEnv.authenticatedContext(CHARLIE_UID).firestore();

  // Seed server_1 first (required by create rule)
  await assertSucceeds(aliceDb.collection("servers").doc("server_1").set(validServerPayload()));

  // Alice invites Bob
  await assertSucceeds(
    aliceDb.collection("trades").doc("trade_1").set(validTradePayload("server_1", ALICE_UID, BOB_UID))
  );

  // Charlie cannot read or list Alice/Bob's trade; unfiltered list fails
  await assertFails(charlieDb.collection("trades").doc("trade_1").get());
  await assertFails(aliceDb.collection("trades").get());
  await assertSucceeds(
    bobDb.collection("trades").where("participantIds", "array-contains", BOB_UID).get()
  );

  // Sender cannot self-accept the pending invite to active; receiver Bob accepts
  await assertFails(
    aliceDb.collection("trades").doc("trade_1").update({
      status: "active",
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertSucceeds(
    bobDb.collection("trades").doc("trade_1").update({
      status: "active",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Both agreeing on empty 0/0 trade MUST fail!
  await assertSucceeds(
    aliceDb.collection("trades").doc("trade_1").update({
      senderAccepted: true,
      senderEscrowPoints: 0,
      senderEscrowCash: 0,
      status: "active",
      updatedAt: new Date(Date.now() - 400),
    })
  );
  await assertFails(
    bobDb.collection("trades").doc("trade_1").update({
      receiverAccepted: true,
      receiverEscrowPoints: 0,
      receiverEscrowCash: 0,
      status: "completed",
      updatedAt: new Date(Date.now() - 350),
    })
  );

  // Alice un-agrees, sets 250 points offer (resets receiverAccepted cleanly)
  await assertSucceeds(
    aliceDb.collection("trades").doc("trade_1").update({
      senderAccepted: false,
      senderEscrowPoints: 0,
      senderEscrowCash: 0,
      status: "active",
      updatedAt: new Date(Date.now() - 300),
    })
  );
  await assertSucceeds(
    aliceDb.collection("trades").doc("trade_1").update({
      senderPoints: 250,
      senderCash: 0,
      receiverAccepted: false,
      updatedAt: new Date(Date.now() - 250),
    })
  );

  // Negative offer rejected
  await assertFails(
    aliceDb.collection("trades").doc("trade_1").update({
      senderPoints: -5,
      senderCash: 0,
      receiverAccepted: false,
      updatedAt: new Date(Date.now() - 240),
    })
  );

  // Alice agrees & escrows 250 points
  await assertSucceeds(
    aliceDb.collection("trades").doc("trade_1").update({
      senderAccepted: true,
      senderEscrowPoints: 250,
      senderEscrowCash: 0,
      status: "active",
      updatedAt: new Date(Date.now() - 200),
    })
  );

  // Bob agrees (0 offered from Bob -> one-sided gift allowed!) -> trade completes!
  await assertSucceeds(
    bobDb.collection("trades").doc("trade_1").update({
      receiverAccepted: true,
      receiverEscrowPoints: 0,
      receiverEscrowCash: 0,
      status: "completed",
      updatedAt: new Date(Date.now() - 150),
    })
  );

  // Terminal state lock: cannot edit offers after completed
  await assertFails(
    aliceDb.collection("trades").doc("trade_1").update({
      senderPoints: 0,
      senderCash: 0,
      receiverAccepted: false,
      updatedAt: new Date(Date.now() - 100),
    })
  );

  // Bob claims once; double claim rejected
  await assertSucceeds(
    bobDb.collection("trades").doc("trade_1").update({
      receiverClaimed: true,
      updatedAt: new Date(Date.now() - 50),
    })
  );
  await assertFails(
    bobDb.collection("trades").doc("trade_1").update({
      receiverClaimed: true,
      updatedAt: new Date(Date.now() - 25),
    })
  );
});

test("Active trade immediate cancel clears canceller escrow and lets partner refund", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(aliceDb.collection("servers").doc("server_1").set(validServerPayload()));
  await assertSucceeds(
    aliceDb.collection("trades").doc("trade_cancel").set(validTradePayload("server_1", ALICE_UID, BOB_UID))
  );
  await assertSucceeds(
    bobDb.collection("trades").doc("trade_cancel").update({
      status: "active",
      updatedAt: new Date(Date.now() - 400),
    })
  );
  await assertSucceeds(
    aliceDb.collection("trades").doc("trade_cancel").update({
      senderPoints: 100,
      senderCash: 5,
      receiverAccepted: false,
      updatedAt: new Date(Date.now() - 300),
    })
  );
  await assertSucceeds(
    aliceDb.collection("trades").doc("trade_cancel").update({
      senderAccepted: true,
      senderEscrowPoints: 100,
      senderEscrowCash: 5,
      status: "active",
      updatedAt: new Date(Date.now() - 200),
    })
  );

  // Bob cancels immediately
  await assertSucceeds(
    bobDb.collection("trades").doc("trade_cancel").update({
      status: "cancelled",
      cancelledBy: BOB_UID,
      receiverAccepted: false,
      receiverEscrowPoints: 0,
      receiverEscrowCash: 0,
      receiverRefunded: true,
      updatedAt: new Date(Date.now() - 100),
    })
  );

  // Alice refunds her escrow once
  await assertSucceeds(
    aliceDb.collection("trades").doc("trade_cancel").update({
      senderRefunded: true,
      senderAccepted: false,
      senderEscrowPoints: 0,
      senderEscrowCash: 0,
      updatedAt: new Date(Date.now() - 50),
    })
  );
});
