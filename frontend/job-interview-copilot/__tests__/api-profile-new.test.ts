import handler from "@/pages/api/profile/new";
import {apiFetchRaw} from "@/lib/api";
import {UnauthorizedError} from "@/error/UnauthorizedError";

jest.mock("@/lib/api/withAuthHeader", () => ({
  withAuthHeader:
    (fn: any) =>
    (req: any, res: any) =>
      fn(req, res, {Authorization: "Bearer token"}),
}));

jest.mock("@/lib/api", () => ({
  apiFetchRaw: jest.fn(),
}));

jest.mock("@/error/UnauthorizedError", () => ({
  UnauthorizedError: class UnauthorizedError extends Error {},
}));

function createRes() {
  const res: any = {};
  res.statusCode = 200;
  res.headers = {};
  res.status = (code: number) => {
    res.statusCode = code;
    return res;
  };
  res.json = (data: any) => {
    res.body = data;
    return res;
  };
  res.send = (data: any) => {
    res.body = data;
    return res;
  };
  res.setHeader = (name: string, value: string[]) => {
    res.headers[name] = value;
  };
  return res;
}

it("rejects non-POST methods", async () => {
  const req: any = {method: "GET"};
  const res = createRes();

  await handler(req, res);

  expect(res.statusCode).toBe(405);
  expect(res.headers.Allow).toEqual(["POST"]);
});

it("proxies POST to profile service", async () => {
  (apiFetchRaw as jest.Mock).mockResolvedValue({
    status: 201,
    headers: new Headers({"content-type": "application/json"}),
    json: async () => ({id: "profile-1"}),
  });

  const req: any = {
    method: "POST",
    body: {displayName: "Primary", pastedCV: "resume"},
  };
  const res = createRes();

  await handler(req, res);

  expect(apiFetchRaw).toHaveBeenCalledWith(
    "/profile/profile",
    expect.objectContaining({
      method: "POST",
      body: req.body,
      headers: {Authorization: "Bearer token"},
    })
  );
  expect(res.statusCode).toBe(201);
  expect(res.body).toEqual({id: "profile-1"});
});

it("returns 401 on UnauthorizedError", async () => {
  (apiFetchRaw as jest.Mock).mockRejectedValue(new UnauthorizedError("Unauthorized"));

  const req: any = {method: "POST", body: {}};
  const res = createRes();

  await handler(req, res);

  expect(res.statusCode).toBe(401);
  expect(res.body).toEqual({error: "Unauthorized"});
});
